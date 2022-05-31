/*
 * File: us_validations.js
 * Description:
 * Contains the specific validations and configuration adjustments for US (897)
 */

var _usSicmenHandler = null;
var _usIsuHandler = null;
var _usSicm = "";
var _kukla = "";
/**
 * Adds the validator for Invoice-to that only 3 address lines can be specified
 */
function addInvoiceAddressLinesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // // no validation for non invoice to
        if (FormManager.getActualValue('addrType') != 'ZI01') {
          return new ValidationResult(null, true);
        }

        var addrLines = 0;
        if (FormManager.getActualValue('addrTxt') != '') {
          addrLines++;
        }
        if (FormManager.getActualValue('addrTxt2') != '') {
          addrLines++;
        }
        if (FormManager.getActualValue('divn') != '') {
          addrLines++;
        }
        if (FormManager.getActualValue('dept') != '') {
          addrLines++;
        }
        if (addrLines > 3) {
          return new ValidationResult(null, false, 'Please fill-out only 3 of the following fields: Address, Address Con\'t, Division, Department.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * Adds the validator for Restrict To
 */
function addInvoiceAddressLinesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getField('restrictInd').get('checked') && FormManager.getActualValue('restrictTo') == '') {
          return new ValidationResult({
            id : 'restrictTo',
            type : 'text',
            name : 'restrictTo'
          }, false, 'Restrict To is required for a Restricted customer.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addCountyValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var id = FormManager.getActualValue('reqId');
        var county = FormManager.getActualValue('county');
        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'U') {
            var result = cmr.query('ADDRESS.GETCOUNTYFORJS', {
              REQ_ID : id,
              ADDR_TYPE : 'ZI01'
            });
            if (result.ret1 == "") {
              return new ValidationResult(null, false, 'County in Invoice-to address is required.');
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), null, 'frmCMR');
}

function addCreateByModelValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var CUST_GRP = FormManager.getActualValue('custSubGrp');
        var CMR_NO = FormManager.getActualValue('modelCmrNo');
        if ((CUST_GRP == 'BYMODEL') && (CMR_NO == '')) {
          return new ValidationResult(null, false, 'A CMR should have been imported on the request as the model CMR.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR');
}

/**
 * Validates that the request only has exactly 1 Install-to and at most 1
 * Invoice-to
 */
function addAddressRecordTypeValidator() {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.USA) {
              return new ValidationResult(null, true);
            }
            if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
              return new ValidationResult(null, false, 'Please add an Install At and optionally an Invoice To address to this request.');
            }
            if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
              var record = null;
              var type = null;
              var invoiceToCnt = 0;
              var installAtCnt = 0;
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                type = record.addrType;
                if (typeof (type) == 'object') {
                  type = type[0];
                }
                if (type == 'ZS01') {
                  installAtCnt++;
                } else if (type == 'ZI01') {
                  invoiceToCnt++;
                }
              }
              // if (installAtCnt != 1 ||
              // CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount >
              // 2) {
              // return new ValidationResult(null, false, 'The request should
              // contain exactly one Install At address and one optional Invoice
              // To
              // address.');
              // }
              if (installAtCnt != 1 || invoiceToCnt > 1) {
                return new ValidationResult(null, false,
                    'The request should contain exactly one Install At address and one optional Invoice To address.');
              } else {
                return new ValidationResult(null, true);
              }
            }
          }
        };
      })(), 'MAIN_NAME_TAB', 'frmCMR');

}

function addCtcObsoleteValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var reqId = FormManager.getActualValue('reqId');
        var clientTier = FormManager.getActualValue('clientTier');
        var oldCtc;
        var qParams = {
          REQ_ID : reqId
        };

        var result = cmr.query('GET.DATA_RDC.CLIENT_TIER_REQID', qParams);
        if (result != null && result != '') {
          oldCtc = result.ret1;
        }

        if (reqType == 'C'
            && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "M" || clientTier == "V" || clientTier == "Z"
                || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C" || clientTier == "0")) {
          return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid value from list.');
        } else if (reqType == 'U'
            && oldCtc != null
            && oldCtc != clientTier
            && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "M" || clientTier == "V" || clientTier == "Z"
                || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C" || clientTier == "0")) {
          return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    }
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/**
 * After configuration for US
 */
function afterConfigForUS() {

  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  _usSicm = FormManager.getActualValue('usSicmen');
  _kukla = FormManager.getActualValue('custClass');
  if (_usSicm.length > 4) {
    _usSicm = _usSicm.substring(0, 4);
    FormManager.setValue('usSicmen', _usSicm);
  }
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Requester' || role == 'Processor') {
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Tax Class / Code 1' ], 'MAIN_CUST_TAB');
  }

  if (reqType == 'U' && role == 'Requester') {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
  }
  if (reqType == 'C' && role == 'Requester' && custGrp == '9' && custSubGrp == 'POA') {
    FormManager.enable('miscBillCd');
  }

  if (reqType == 'C' && role == 'Requester' && custGrp == '1' && custSubGrp == 'ECOSYSTEM') {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Y');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  } else {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
  }

  if (_pagemodel.reqType == 'U') {
    FormManager.show('CustClass', 'custClass');
    if (FormManager.getActualValue('userRole').toUpperCase() == 'VIEWER') {
      FormManager.readOnly('custClass');
    } else {
      FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Classification Code' ], 'MAIN_CUST_TAB');
    }
  } else {
    FormManager.removeValidator('custClass', Validators.REQUIRED);
    FormManager.hide('CustClass', 'custClass');
  }
  // Enterprise field as mandatory for BP scenario
  var custTypeHandler = null;
  if (custTypeHandler == null) {
    var custTypeHandler = dojo.connect(FormManager.getField('custType'), 'onChange', function(value) {
      if (FormManager.getActualValue('userRole').toUpperCase() == 'REQUESTER' && FormManager.getActualValue('reqType') == 'C'
          && FormManager.getActualValue('custType') == '7') {
        FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('enterprise', Validators.REQUIRED);
      }
    });
  }

  var usCntryHandler = null;
  if (usCntryHandler == null) {
    // set the postal code to 0000 for non-US landed countries and disable
    usCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      if (FormManager.getActualValue('landCntry') != '' && FormManager.getActualValue('landCntry') != 'US') {
        FormManager.setValue('postCd', '00000');
        FormManager.readOnly('postCd');
      } else {
        var readOnly = false;
        try {
          if (TemplateService.isHandled('postCd')) {
            if ((typeof TemplateService != "undefined") && (typeof PageManager != "undefined")) {
              readOnly = TemplateService.isFieldReadOnly('postCd');
              if ((readOnly || PageManager.isReadOnly())) {
                FormManager.readOnly('postCd');
              } else {
                FormManager.enable('postCd');
              }
            }
          }
        } catch (e) {
          FormManager.enable('postCd');
        }
      }
    });
  }

  if (_usSicmenHandler == null) {
    _usSicmenHandler = dojo.connect(FormManager.getField('usSicmen'), 'onChange', function(value) {
      var sicmen = FormManager.getActualValue('usSicmen');
      _usSicm = FormManager.getActualValue('usSicmen');
      var _custType = FormManager.getActualValue('custSubGrp');
      if (_custType == 'OEMHW' || _custType == 'OEM-SW' || _custType == 'TPD' || _custType == 'SSD' || _custType == 'DB4') {
        FormManager.setValue('isicCd', '357X');
      } else {
        var currIsic = FormManager.getActualValue('isicCd');
        if (currIsic != '357X') {
          if (_usSicm.length > 4) {
            _usSicm = _usSicm.substring(0, 4);
          }
          FormManager.setValue('isicCd', _usSicm);
        }
      }
    });
  }
  _usSicmenHandler[0].onChange();

  if (reqType == 'U') {
    FormManager.readOnly('custType');
  }

  if (_usIsuHandler == null && FormManager.getField('isuCd')) {
    _usIsuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValuesUS();
    });
  }

}

function initUSTemplateHandler() {
  // templates/scenarios initialization. connect onchange of the subgroup
  // to load the template
  if (_templateHandler == null && FormManager.getField('custSubGrp')) {
    _templateHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (value && value != '') {
        TemplateService.fill('reqentry');
      }
    });
  }
  if (_templateHandler && _templateHandler[0]) {
    _templateHandler[0].onChange();
  }
}

function addUSAddressHandler(cntry, addressMode, saving) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = 'US';
      FormManager.setValue('landCntry', 'US');
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function toggleAddrTypesForUS(cntry, addressMode, details) {
  var reqType = FormManager.getActualValue('reqType');
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
      var firstRecord = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(0);
      var type = firstRecord.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }
      if (type == 'ZS01') {
        FormManager.setValue('addrType', 'ZI01');
        cmr.showNode('radiocont_ZI01');
        cmr.hideNode('radiocont_ZS01');
      } else if (type == 'ZI01') {
        FormManager.setValue('addrType', 'ZS01');
        cmr.showNode('radiocont_ZS01');
        cmr.hideNode('radiocont_ZI01');
      }
    } else {
      FormManager.setValue('addrType', 'ZS01');
      cmr.showNode('radiocont_ZS01');
      cmr.hideNode('radiocont_ZI01');
    }
  }
  // CMR - 6072
  if (reqType == 'U' && (cmr.addressMode == 'updateAddress' || cmr.addressMode == 'copyAddress')) {
    cmr.showNode('radiocont_ZP01');
  } else {
    cmr.hideNode('radiocont_ZP01');
  }
}

function setCSPValues(fromAddress, scenario, scenarioChanged) {
  if (scenario == 'CSP') {
    FormManager.setValue('isuCd', '32');
    FormManager.setValue('clientTier', 'N');
    FormManager.readOnly('isuCd');
  } else {
    FormManager.enable('isuCd');
  }
}

function enableUSSicMenForScenarios(fromAddress, scenario, scenarioChanged) {
  var reqType = FormManager.getActualValue('reqType');
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C') {
    FormManager.readOnly('isicCd');
  } else if (reqType == 'U') {
    if (role == 'Processor') {
      FormManager.enable('isicCd');
    } else {
      FormManager.readOnly('isicCd');
    }
  } else {
    FormManager.readOnly('isicCd');
  }
}

function canUpdateAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType;
  if (addrType == 'ZP01' && role == 'REQUESTER') {
    return false;
  } else {
    return true;
  }
}

function canCopyAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType;
  if (addrType == 'ZP01') {
    return false;
  } else {
    return true;
  }
}

function canRemoveAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType;
  if (addrType == 'ZP01') {
    return false;
  } else {
    return true;
  }
}

function setClientTierValuesUS() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.resetValidations('clientTier');
  } else {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var reqType = FormManager.getActualValue('reqType');
    if (reqType == 'U' || (reqType != 'U' && userRole == 'PROCESSOR')) {
      FormManager.enable('clientTier');
    }
  }
}

// CREATCMR-3298
function checkSCCValidate() {
  var landCntry = '';
  var st = '';
  var cnty = '';
  var city = '';

  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {

      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      type = record.addrType;

      if (typeof (type) == 'object') {
        type = type[0];
      }

      if (type == 'ZS01') {
        landCntry = record.landCntry;
        st = record.stateProv;
        cnty = record.county;
        city = record.city1.toString().toUpperCase();
      }
    }

    var numeric = /^[0-9]*$/;

    if (numeric.test(cnty)) {

      var ret = cmr.query('US_CMR_SCC.GET_SCC_MULTIPLE_BY_LAND_CNTRY_ST_CITY', {
        _qall : 'Y',
        LAND_CNTRY : landCntry,
        N_ST : st,
        N_CITY : city
      });

      if (ret.length > 1) {
        $("#addressTabSccInfo").html('');
        $('#sccMultipleWarn').show();
      }

      var ret1 = cmr.query('US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY', {
        LAND_CNTRY : landCntry,
        N_ST : st,
        C_CNTY : cnty,
        N_CITY : city
      });

      var sccValue = '';

      if (ret1 && ret1.ret1 && ret1.ret1 != '') {
        sccValue = ret1.ret1;
        // CREATCMR-5447
        $("#addressTabSccInfo").html(sccValue);
      } else {
        $('#sccWarn').show();
      }

    } else {
      $('#sccWarn').show();
    }
  }
}

function sccWarningShowAndHide() {
  var action = FormManager.getActualValue('yourAction');

  if (action == 'SAV' || action == 'SFP') {
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          checkSCCValidate();
        }
      }
    })(), 'MAIN_NAME_TAB', 'frmCMR');
  }

}

function usRestrictCode() {
  if (FormManager.getActualValue('custSubGrp') == 'KYN') {
    FormManager.setValue('inacType', 'I');
    FormManager.setValue('inacCd', '6272');
    FormManager.setValue('mtkgArDept', 'SD3');
    FormManager.setValue('svcArOffice', 'IJ9');
    FormManager.readOnly('inacType');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('mtkgArDept');
    FormManager.readOnly('svcArOffice');
  }
}

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');

  if (isuCode == '5K') {
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
  }
}

function clientTierValidator() {
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
            oldISU =  result.ret3 != null ? result.ret3 : '';
            
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

function addKuklaValidator() {

  var kuklaOfArray = [ '21', '71', '99', '85', '43', '34', '52' ];
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var kukla = FormManager.getActualValue('custClass');
        var ret = cmr.query('DATA_RDC.CUST_CLASS', {
          REQ_ID : FormManager.getActualValue('reqId')
        });
        if (ret && ret.ret1 && ret.ret1 != '') {
          _kukla =ret.ret1;
        }
        if (FormManager.getActualValue('reqType') == 'U' && kukla != _kukla) {
          if (kuklaOfArray.includes(kukla)) {
            return new ValidationResult(null, false, 'KUKLA ' + kukla + ' should not be used for update');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addDivStreetCountValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('addrType') != 'ZP01' && FormManager.getActualValue('addrType') != 'ZS01'
            && FormManager.getActualValue('addrType') != 'ZI01') {
          return new ValidationResult(null, true);
        }
        var count = 0;
        if (FormManager.getActualValue('addrTxt2') != '') {
          count++;
        }
        if (FormManager.getActualValue('divn') != '') {
          count++;
        }
        if (count > 1) {
          return new ValidationResult(null, false, 'Address Cont and Division can\'t be filled at the same time.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function hideKUKLA() {
  if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CustClass', 'custClass');
    FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Classification Code' ], 'MAIN_CUST_TAB');
    if (FormManager.getActualValue('userRole').toUpperCase() == 'VIEWER') {
      FormManager.readOnly('custClass');
    }
  } else {
    FormManager.removeValidator('custClass', Validators.REQUIRED);
    FormManager.hide('CustClass', 'custClass');
  }
}

function lockOrdBlk() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // if (role == 'REQUESTER') {
  // FormManager.enable('ordBlk');
  // } else {
  // FormManager.enable('ordBlk');
  // }
  if (reqType == 'U') {
    FormManager.enable('ordBlk');
  } else {
    FormManager.readOnly('ordBlk');
  }
}

function orderBlockValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var ordBlk = FormManager.getActualValue('ordBlk');
        if (ordBlk != '') {
          if (ordBlk.startsWith('8') || ordBlk.startsWith('9')) {
          } else {
            return new ValidationResult(null, false, 'Only blank, 88, 90 are allowed.');
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-5447
function TaxTeamUpdateAddrValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'U') {

          var chkResult = false;
          var isOutTaxAddrChange = false;
          var isTaxTeamUser = '';
          var result = {};
          isTaxTeamUser = FormManager.getActualValue('isTaxTeamFlg');

          result = cmr.query('US_TAXTEAM.OUT_ADDR_TAXTEAM', {
            REQ_ID : FormManager.getActualValue('reqId')
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined) {
            isOutTaxAddrChange = true;
          }

          if (isTaxTeamUser == 'true') {
            if (!isOutTaxAddrChange) {
              chkResult = true;
            }
          }
          if (chkResult) {
            return new ValidationResult(null, false, 'You can only update SCC fields. Please check current data and try again later.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function TaxTeamUpdateDataValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'U') {

          var chkResult = false;
          var isTaxTeamUser = '';
          var isInTaxDataChange = false;
          var isOutTaxDataChange = false;
          var result = {};
          isTaxTeamUser = FormManager.getActualValue('isTaxTeamFlg');

          result = cmr.query('US_TAXTEAM.IN_TAXTEAM', {
            REQ_ID : FormManager.getActualValue('reqId')
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined) {
            isInTaxDataChange = true;
          }
          result = cmr.query('US_TAXTEAM.OUT_TAXTEAM', {
            REQ_ID : FormManager.getActualValue('reqId')
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined) {
            isOutTaxDataChange = true;
          }
          if (isTaxTeamUser == 'true') {
            if (!isOutTaxDataChange) {
              chkResult = true;
            }
          }
          if (chkResult) {
            return new ValidationResult(null, false, 'You can only update the Tax fields. Please check current data and try again later.');
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

// CREATCMR-4466
function addCompanyEnterpriseValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var custNm = FormManager.getActualValue('mainCustNm1') + FormManager.getActualValue('mainCustNm2');
        var company = FormManager.getActualValue('company');
        var enterprise = FormManager.getActualValue('enterprise');

        custNm = custNm.toUpperCase().replaceAll(' ', '');

        var chkResult = false;
        var ret = {};

        if (custNm != '') {
          // a. Enterprise and Company both specified on request
          if (enterprise != '' && company != '') {
            if (FormManager.getActualValue('reqType') == 'C') {
              ret = cmr.query('US_COMPANY.GET_ENT_NO', {
                COMP_NO : company,
                COMP_LEGAL_NAME : custNm,
                ENT_NO : enterprise,
                MANDT : cmr.MANDT
              });
              if (ret && ret.ret1 && ret.ret1 != '') {
                chkResult = false;
              } else {
                chkResult = true;
              }
            } else if (FormManager.getActualValue('reqType') == 'U') {
              ret = cmr.query('US_ENTERPRISE.GET_ENT_NO', {
                ENT_NO : enterprise,
                MANDT : cmr.MANDT
              });
              if (ret && ret.ret1 && ret.ret1 != '') {
                ret = cmr.query('US_COMPANY.GET_COMP', {
                  _qall : 'Y',
                  ENT_NO : enterprise,
                  COMP_LEGAL_NAME : custNm,
                  MANDT : cmr.MANDT
                });
                if (ret != null) {
                  var tempCompNo = '';
                  var hasCompNo = false;
                  for (var i = 0; i < ret.length; i++) {
                    tempCompNo = ret[0].ret1;
                    if (ret[i].ret1 == company) {
                      hasCompNo = true;
                      break;
                    }
                  }
                  if (!hasCompNo && tempCompNo != '') {
                    FormManager.setValue('company', tempCompNo);
                  }
                }
              } else {
                chkResult = true;
              }
            }
          } else if (enterprise != '') {
            // b. Enterprise specified on request
            ret = cmr.query('US_ENTERPRISE.GET_ENT_NO', {
              ENT_NO : enterprise,
              MANDT : cmr.MANDT
            });
            if (ret && ret.ret1 && ret.ret1 != '') {
              ret = cmr.query('US_COMPANY.GET_COMP', {
                ENT_NO : enterprise,
                COMP_LEGAL_NAME : custNm,
                MANDT : cmr.MANDT
              });
              if (ret && ret.ret1 && ret.ret1 != '') {
                FormManager.setValue('company', ret.ret1);
              }
            } else {
              chkResult = true;
            }
          } else if (company != '') {
            // c. Company specified on request
            ret = cmr.query('US_COMPANY.GET_ENT_COMP', {
              COMP_NO : company,
              COMP_LEGAL_NAME : custNm,
              MANDT : cmr.MANDT
            });
            if (ret && ret.ret1 && ret.ret1 != '') {
              // CREATCMR-5907
              if (FormManager.getActualValue('reqType') == 'C') {
                FormManager.setValue('enterprise', ret.ret1);
              }
            } else {
              chkResult = true;
            }
          } else if (company == '' && enterprise == '') {
            // d. None specified on request
            ret = cmr.query('US_COMPANY.COMP_LEGAL_NAME', {
              COMP_LEGAL_NAME : custNm,
              MANDT : cmr.MANDT
            });
            if (ret && ret.ret1 && ret.ret1 != '') {
              FormManager.setValue('enterprise', ret.ret1);
              FormManager.setValue('company', ret.ret2);
            }
          }
        } else {
          chkResult = true;
        }
        if (chkResult) {
          return new ValidationResult(null, false, 'The Company Number or Enterprise Number cannot be found.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}
/* Register US Javascripts */
dojo.addOnLoad(function() {
  console.log('adding US scripts...');
  GEOHandler.registerValidator(addInvoiceAddressLinesValidator, [ SysLoc.USA ], null, true);
  // GEOHandler.registerValidator(addCountyValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addCreateByModelValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addAddressRecordTypeValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addCtcObsoleteValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.USA ], null, true);
  GEOHandler.addAfterConfig(afterConfigForUS, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(afterConfigForUS, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(initUSTemplateHandler, [ SysLoc.USA ]);
  GEOHandler.addAddrFunction(addUSAddressHandler, [ SysLoc.USA ]);
  GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForUS, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(setCSPValues, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(enableUSSicMenForScenarios, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(enableUSSicMenForScenarios, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(usRestrictCode, [ SysLoc.USA ]);

  /* requireDPL check ad assessment for all users */
  GEOHandler.registerValidator(addDPLCheckValidator, [ SysLoc.USA ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLAssessmentValidator, [ SysLoc.USA ], null, true);
  // CREATCMR-4466
  // GEOHandler.registerValidator(addCompanyEnterpriseValidation, [ SysLoc.USA
  // ], null, true);
  GEOHandler.addAfterConfig(lockOrdBlk, [ SysLoc.USA ]);
  GEOHandler.registerValidator(orderBlockValidation, [ SysLoc.USA ], null, true);

  // CREATCMR-3298
  GEOHandler.addAfterConfig(checkSCCValidate, [ SysLoc.USA ]);
  GEOHandler.registerValidator(sccWarningShowAndHide, [ SysLoc.USA ], null, false);

  GEOHandler.addAddrFunction(hideKUKLA, [ SysLoc.USA ]);
  GEOHandler.registerValidator(addKuklaValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addDivStreetCountValidator, [ SysLoc.USA ], null, true);

  GEOHandler.addAfterTemplateLoad(setClientTierValuesUS, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(setClientTierValuesUS, [ SysLoc.USA ]);
  // CREATCMR-5447
  GEOHandler.registerValidator(TaxTeamUpdateDataValidation, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(TaxTeamUpdateAddrValidation, [ SysLoc.USA ], null, true);
});