/*
 * File: us_validations.js
 * Description:
 * Contains the specific validations and configuration adjustments for US (897)
 */

var _usSicmenHandler = null;
var _usIsuHandler = null;
var _usSicm = "";
var _kukla = "";
var _enterpriseHandler = null;
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
          // CREATCMR-6587
          setAffiliateNumber();
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

  if (_enterpriseHandler == null) {
    _enterpriseHandler = dojo.connect(FormManager.getField('enterprise'), 'onChange', function(value) {
      updateBOForEntp();
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
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('clientTier');
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

    if (cnty != '') {
      if (numeric.test(cnty)) {

        var role = null;
        if (typeof (_pagemodel) != 'undefined') {
          role = _pagemodel.userRole;
        }

        if (role == 'Processor') {
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
          _kukla = ret.ret1;
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
            && FormManager.getActualValue('addrType') != 'ZI01' || FormManager.getActualValue('bpAcctTyp') == 'E') {
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

        ret = cmr.query('BP.GET_PROCESSING_TYP', {
          CNTRY_CD : SysLoc.USA
        });
        if (ret && ret.ret1 && ret.ret1 != '' && ret.ret1 == 'TC') {
          return new ValidationResult(null, true);
        }
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

// CREATCMR-3440 - BO Computation Update - Entp - 6500871
function updateBOForEntp() {
  if (FormManager.getActualValue('enterprise') == '6500871') {
    FormManager.setValue('csoSite', 'PAH');
    FormManager.setValue('svcArOffice', 'IJ9');
    FormManager.setValue('mtkgArDept', 'SD3');
    FormManager.setValue('mktgDept', 'SVB');
  }
}

// CREATCMR-6587
function setAffiliateNumber() {
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  var isicCd = FormManager.getActualValue('isicCd');
  if (subIndustryCd.startsWith('Y') && (isicCd.startsWith('90') || isicCd.startsWith('91') || isicCd.startsWith('92'))) {
    if (isicCd == '9001') {
      FormManager.setValue('affiliate', '0089800');
    } else if (isicCd == '9002') {
      FormManager.setValue('affiliate', '0084800');
    } else if (isicCd == '9003') {
      FormManager.setValue('affiliate', '0086100');
    } else if (isicCd == '9004') {
      FormManager.setValue('affiliate', '0085900');
    } else if (isicCd == '9005') {
      FormManager.setValue('affiliate', '0086000');
    } else if (isicCd == '9006') {
      FormManager.setValue('affiliate', '0085800');
    } else if (isicCd == '9007') {
      FormManager.setValue('affiliate', '0080200');
    } else if (isicCd == '9008') {
      FormManager.setValue('affiliate', '0085300');
    } else if (isicCd == '9009') {
      FormManager.setValue('affiliate', '0085600');
    } else if (isicCd == '9010') {
      FormManager.setValue('affiliate', '0080300');
    } else if (isicCd == '9011') {
      FormManager.setValue('affiliate', '0088300');
    } else if (isicCd == '9012') {
      FormManager.setValue('affiliate', '0086900');
    } else if (isicCd == '9013') {
      FormManager.setValue('affiliate', '0082200');
    } else if (isicCd == '9014') {
      FormManager.setValue('affiliate', '0082100');
    } else if (isicCd == '9015') {
      FormManager.setValue('affiliate', '0082700');
    } else if (isicCd == '9016') {
      FormManager.setValue('affiliate', '0085300');
    } else if (isicCd == '9017') {
      FormManager.setValue('affiliate', '0082800');
    } else if (isicCd == '9018') {
      FormManager.setValue('affiliate', '0082900');
    } else if (isicCd == '9019') {
      FormManager.setValue('affiliate', '0082600');
    } else if (isicCd == '9020') {
      FormManager.setValue('affiliate', '0083400');
    } else if (isicCd == '9021') {
      FormManager.setValue('affiliate', '0083100');
    } else if (isicCd == '9022') {
      FormManager.setValue('affiliate', '0083200');
    } else if (isicCd == '9023') {
      FormManager.setValue('affiliate', '0082500');
    } else if (isicCd == '9024') {
      FormManager.setValue('affiliate', '0083000');
    } else if (isicCd == '9025') {
      FormManager.setValue('affiliate', '0088600');
    } else if (isicCd == '9026') {
      FormManager.setValue('affiliate', '0088800');
    } else if (isicCd == '9027') {
      FormManager.setValue('affiliate', '0083300');
    } else if (isicCd == '9028') {
      FormManager.setValue('affiliate', '0083100');
    } else if (isicCd == '9029') {
      FormManager.setValue('affiliate', '0086400');
    } else if (isicCd == '9030') {
      FormManager.setValue('affiliate', '0081900');
    } else if (isicCd == '9031') {
      FormManager.setValue('affiliate', '0081000');
    } else if (isicCd == '9032') {
      FormManager.setValue('affiliate', '0087900');
    } else if (isicCd == '9033') {
      FormManager.setValue('affiliate', '0085500');
    } else if (isicCd == '9034') {
      FormManager.setValue('affiliate', '0084200');
    } else if (isicCd == '9035') {
      FormManager.setValue('affiliate', '0084300');
    } else if (isicCd == '9036') {
      FormManager.setValue('affiliate', '0084700');
    } else if (isicCd == '9037') {
      FormManager.setValue('affiliate', '0084400');
    } else if (isicCd == '9038') {
      FormManager.setValue('affiliate', '0076500');
    } else if (isicCd == '9040') {
      FormManager.setValue('affiliate', '0084900');
    } else if (isicCd == '9041') {
      FormManager.setValue('affiliate', '0085000');
    } else if (isicCd == '9042') {
      FormManager.setValue('affiliate', '0089300');
    } else if (isicCd == '9043') {
      FormManager.setValue('affiliate', '0089400');
    } else if (isicCd == '9044') {
      FormManager.setValue('affiliate', '0089000');
    } else if (isicCd == '9045') {
      FormManager.setValue('affiliate', '0089500');
    } else if (isicCd == '9046') {
      FormManager.setValue('affiliate', '0060100');
    } else if (isicCd == '9048') {
      FormManager.setValue('affiliate', '0062100');
    } else if (isicCd == '9049') {
      FormManager.setValue('affiliate', '0069300');
    } else if (isicCd == '9050') {
      FormManager.setValue('affiliate', '0066600');
    } else if (isicCd == '9051') {
      FormManager.setValue('affiliate', '0069600');
    } else if (isicCd == '9052') {
      FormManager.setValue('affiliate', '0074800');
    } else if (isicCd == '9053') {
      FormManager.setValue('affiliate', '0082300');
    } else if (isicCd == '9054') {
      FormManager.setValue('affiliate', '0080600');
    } else if (isicCd == '9055') {
      FormManager.setValue('affiliate', '0060600');
    } else if (isicCd == '9056') {
      FormManager.setValue('affiliate', '0080400');
    } else if (isicCd == '9057') {
      FormManager.setValue('affiliate', '0088400');
    } else if (isicCd == '9058') {
      FormManager.setValue('affiliate', '0088500');
    } else if (isicCd == '9060') {
      FormManager.setValue('affiliate', '0062300');
    } else if (isicCd == '9061') {
      FormManager.setValue('affiliate', '0062400');
    } else if (isicCd == '9063') {
      FormManager.setValue('affiliate', '0062600');
    } else if (isicCd == '9064') {
      FormManager.setValue('affiliate', '0062700');
    } else if (isicCd == '9065') {
      FormManager.setValue('affiliate', '0088700');
    } else if (isicCd == '9066') {
      FormManager.setValue('affiliate', '0062200');
    } else if (isicCd == '9067') {
      FormManager.setValue('affiliate', '0061300');
    } else if (isicCd == '9068') {
      FormManager.setValue('affiliate', '0061400');
    } else if (isicCd == '9069') {
      FormManager.setValue('affiliate', '0061500');
    } else if (isicCd == '9070') {
      FormManager.setValue('affiliate', '0085700');
    } else if (isicCd == '9071') {
      FormManager.setValue('affiliate', '0061800');
    } else if (isicCd == '9072') {
      FormManager.setValue('affiliate', '0060200');
    } else if (isicCd == '9073') {
      FormManager.setValue('affiliate', '0060300');
    } else if (isicCd == '9074') {
      FormManager.setValue('affiliate', '0060400');
    } else if (isicCd == '9075') {
      FormManager.setValue('affiliate', '0060500');
    } else if (isicCd == '9076') {
      FormManager.setValue('affiliate', '0069000');
    } else if (isicCd == '9077') {
      FormManager.setValue('affiliate', '0060700');
    } else if (isicCd == '9078') {
      FormManager.setValue('affiliate', '0060800');
    } else if (isicCd == '9079') {
      FormManager.setValue('affiliate', '0061100');
    } else if (isicCd == '9080') {
      FormManager.setValue('affiliate', '0075200');
    } else if (isicCd == '9081') {
      FormManager.setValue('affiliate', '0061600');
    } else if (isicCd == '9083') {
      FormManager.setValue('affiliate', '0062000');
    } else if (isicCd == '9084') {
      FormManager.setValue('affiliate', '0061900');
    } else if (isicCd == '9085') {
      FormManager.setValue('affiliate', '0080500');
    } else if (isicCd == '9086') {
      FormManager.setValue('affiliate', '0074000');
    } else if (isicCd == '9087') {
      FormManager.setValue('affiliate', '0061700');
    } else if (isicCd == '9088') {
      FormManager.setValue('affiliate', '0060900');
    } else if (isicCd == '9089') {
      FormManager.setValue('affiliate', '0061000');
    } else if (isicCd == '9093') {
      FormManager.setValue('affiliate', '0066000');
    } else if (isicCd == '9095') {
      FormManager.setValue('affiliate', '0065300');
    } else if (isicCd == '9096') {
      FormManager.setValue('affiliate', '0061200');
    } else if (isicCd == '9098') {
      FormManager.setValue('affiliate', '0066800');
    } else if (isicCd == '9099') {
      FormManager.setValue('affiliate', '0076800');
    } else if (isicCd == '9100') {
      FormManager.setValue('affiliate', '0067200');
    } else if (isicCd == '9102') {
      FormManager.setValue('affiliate', '0068600');
    } else if (isicCd == '9104') {
      FormManager.setValue('affiliate', '0068800');
    } else if (isicCd == '9105') {
      FormManager.setValue('affiliate', '0068900');
    } else if (isicCd == '9108') {
      FormManager.setValue('affiliate', '0069200');
    } else if (isicCd == '9109') {
      FormManager.setValue('affiliate', '0065000');
    } else if (isicCd == '9110') {
      FormManager.setValue('affiliate', '0063700');
    } else if (isicCd == '9111') {
      FormManager.setValue('affiliate', '0066800');
    } else if (isicCd == '9112') {
      FormManager.setValue('affiliate', '0064100');
    } else if (isicCd == '9113') {
      FormManager.setValue('affiliate', '0069700');
    } else if (isicCd == '9115') {
      FormManager.setValue('affiliate', '0064200');
    } else if (isicCd == '9117') {
      FormManager.setValue('affiliate', '0073100');
    } else if (isicCd == '9119') {
      FormManager.setValue('affiliate', '0073300');
    } else if (isicCd == '9120') {
      FormManager.setValue('affiliate', '0073500');
    } else if (isicCd == '9121') {
      FormManager.setValue('affiliate', '0064300');
    } else if (isicCd == '9122') {
      FormManager.setValue('affiliate', '0073700');
    } else if (isicCd == '9124') {
      FormManager.setValue('affiliate', '0073900');
    } else if (isicCd == '9125') {
      FormManager.setValue('affiliate', '0064400');
    } else if (isicCd == '9126') {
      FormManager.setValue('affiliate', '0064500');
    } else if (isicCd == '9127') {
      FormManager.setValue('affiliate', '0064600');
    } else if (isicCd == '9129') {
      FormManager.setValue('affiliate', '0070100');
    } else if (isicCd == '9130') {
      FormManager.setValue('affiliate', '0064700');
    } else if (isicCd == '9131') {
      FormManager.setValue('affiliate', '0070300');
    } else if (isicCd == '9132') {
      FormManager.setValue('affiliate', '0064800');
    } else if (isicCd == '9134') {
      FormManager.setValue('affiliate', '0071000');
    } else if (isicCd == '9135') {
      FormManager.setValue('affiliate', '0064900');
    } else if (isicCd == '9136') {
      FormManager.setValue('affiliate', '0071200');
    } else if (isicCd == '9137') {
      FormManager.setValue('affiliate', '0071300');
    } else if (isicCd == '9138') {
      FormManager.setValue('affiliate', '0071400');
    } else if (isicCd == '9139') {
      FormManager.setValue('affiliate', '0065400');
    } else if (isicCd == '9140') {
      FormManager.setValue('affiliate', '0065500');
    } else if (isicCd == '9141') {
      FormManager.setValue('affiliate', '0072000');
    } else if (isicCd == '9142') {
      FormManager.setValue('affiliate', '0082400');
    } else if (isicCd == '9147') {
      FormManager.setValue('affiliate', '0079700');
    } else if (isicCd == '9148') {
      FormManager.setValue('affiliate', '0078600');
    } else if (isicCd == '9151') {
      FormManager.setValue('affiliate', '0078900');
    } else if (isicCd == '9153') {
      FormManager.setValue('affiliate', '0078300');
    } else if (isicCd == '9155') {
      FormManager.setValue('affiliate', '0078400');
    } else if (isicCd == '9156') {
      FormManager.setValue('affiliate', '0078500');
    } else if (isicCd == '9157') {
      FormManager.setValue('affiliate', '0078700');
    } else if (isicCd == '9158') {
      FormManager.setValue('affiliate', '0078800');
    } else if (isicCd == '9159') {
      FormManager.setValue('affiliate', '0079100');
    } else if (isicCd == '9160') {
      FormManager.setValue('affiliate', '0062100');
    } else if (isicCd == '9162') {
      FormManager.setValue('affiliate', '0065600');
    } else if (isicCd == '9163') {
      FormManager.setValue('affiliate', '0065800');
    } else if (isicCd == '9164') {
      FormManager.setValue('affiliate', '0065900');
    } else if (isicCd == '9165') {
      FormManager.setValue('affiliate', '0080100');
    } else if (isicCd == '9166') {
      FormManager.setValue('affiliate', '0066200');
    } else if (isicCd == '9167') {
      FormManager.setValue('affiliate', '0066300');
    } else if (isicCd == '9168') {
      FormManager.setValue('affiliate', '0066500');
    } else if (isicCd == '9169') {
      FormManager.setValue('affiliate', '0066700');
    } else if (isicCd == '9170') {
      FormManager.setValue('affiliate', '0067400');
    } else if (isicCd == '9171') {
      FormManager.setValue('affiliate', '0067500');
    } else if (isicCd == '9172') {
      FormManager.setValue('affiliate', '0067600');
    } else if (isicCd == '9173') {
      FormManager.setValue('affiliate', '0067700');
    } else if (isicCd == '9174') {
      FormManager.setValue('affiliate', '0067800');
    } else if (isicCd == '9175') {
      FormManager.setValue('affiliate', '0068100');
    } else if (isicCd == '9176') {
      FormManager.setValue('affiliate', '0067900');
    } else if (isicCd == '9178') {
      FormManager.setValue('affiliate', '0068200');
    } else if (isicCd == '9179') {
      FormManager.setValue('affiliate', '0068300');
    } else if (isicCd == '9180') {
      FormManager.setValue('affiliate', '0068400');
    } else if (isicCd == '9181') {
      FormManager.setValue('affiliate', '0068500');
    } else if (isicCd == '9182') {
      FormManager.setValue('affiliate', '0069400');
    } else if (isicCd == '9183') {
      FormManager.setValue('affiliate', '0069500');
    } else if (isicCd == '9184') {
      FormManager.setValue('affiliate', '0086100');
    } else if (isicCd == '9185') {
      FormManager.setValue('affiliate', '0076500');
    } else if (isicCd == '9186') {
      FormManager.setValue('affiliate', '0076600');
    } else if (isicCd == '9187') {
      FormManager.setValue('affiliate', '0076700');
    } else if (isicCd == '9189') {
      FormManager.setValue('affiliate', '0075500');
    } else if (isicCd == '9191') {
      FormManager.setValue('affiliate', '0075700');
    } else if (isicCd == '9192') {
      FormManager.setValue('affiliate', '0071100');
    } else if (isicCd == '9193') {
      FormManager.setValue('affiliate', '0075900');
    } else if (isicCd == '9194') {
      FormManager.setValue('affiliate', '0070700');
    } else if (isicCd == '9195') {
      FormManager.setValue('affiliate', '0076100');
    } else if (isicCd == '9196') {
      FormManager.setValue('affiliate', '0071500');
    } else if (isicCd == '9197') {
      FormManager.setValue('affiliate', '0071600');
    } else if (isicCd == '9198') {
      FormManager.setValue('affiliate', '0076400');
    } else if (isicCd == '9199') {
      FormManager.setValue('affiliate', '0075100');
    } else if (isicCd == '9200') {
      FormManager.setValue('affiliate', '0076500');
    } else if (isicCd == '9201') {
      FormManager.setValue('affiliate', '0075000');
    } else if (isicCd == '9203') {
      FormManager.setValue('affiliate', '0085100');
    } else if (isicCd == '9204') {
      FormManager.setValue('affiliate', '0085100');
    } else if (isicCd == '9207') {
      FormManager.setValue('affiliate', '0084100');
    } else if (isicCd == '9208') {
      FormManager.setValue('affiliate', '0085100');
    } else if (isicCd == '9209') {
      FormManager.setValue('affiliate', '0085200');
    } else if (isicCd == '9210') {
      FormManager.setValue('affiliate', '0086200');
    } else if (isicCd == '9211') {
      FormManager.setValue('affiliate', '0086500');
    } else if (isicCd == '9213') {
      FormManager.setValue('affiliate', '0087100');
    } else if (isicCd == '9214') {
      FormManager.setValue('affiliate', '0087200');
    } else if (isicCd == '9215') {
      FormManager.setValue('affiliate', '0087300');
    } else if (isicCd == '9216') {
      FormManager.setValue('affiliate', '0086700');
    } else if (isicCd == '9218') {
      FormManager.setValue('affiliate', '0086100');
    } else if (isicCd == '9219') {
      FormManager.setValue('affiliate', '0086800');
    } else if (isicCd == '9222') {
      FormManager.setValue('affiliate', '0062800');
    } else if (isicCd == '9223') {
      FormManager.setValue('affiliate', '0062900');
    } else if (isicCd == '9224') {
      FormManager.setValue('affiliate', '0063000');
    } else if (isicCd == '9226') {
      FormManager.setValue('affiliate', '0063200');
    } else if (isicCd == '9227') {
      FormManager.setValue('affiliate', '0063300');
    } else if (isicCd == '9228') {
      FormManager.setValue('affiliate', '0063400');
    } else if (isicCd == '9230') {
      FormManager.setValue('affiliate', '0089700');
    } else if (isicCd == '9231') {
      FormManager.setValue('affiliate', '0089900');
    } else if (isicCd == '9232') {
      FormManager.setValue('affiliate', '0085200');
    } else if (isicCd == '9234') {
      FormManager.setValue('affiliate', '0075300');
    } else if (isicCd == '9235') {
      FormManager.setValue('affiliate', '0088900');
    } else if (isicCd == '9236') {
      FormManager.setValue('affiliate', '0089200');
    } else if (isicCd == '9237') {
      FormManager.setValue('affiliate', '0089600');
    } else if (isicCd == '9239') {
      FormManager.setValue('affiliate', '0072700');
    } else if (isicCd == '9240') {
      FormManager.setValue('affiliate', '0072900');
    } else if (isicCd == '9241') {
      FormManager.setValue('affiliate', '0073000');
    } else if (isicCd == '9242') {
      FormManager.setValue('affiliate', '0074300');
    } else if (isicCd == '9243') {
      FormManager.setValue('affiliate', '0088000');
    } else if (isicCd == '9244') {
      FormManager.setValue('affiliate', '0079900');
    } else if (isicCd == '9245') {
      FormManager.setValue('affiliate', '0085400');
    } else if (isicCd == '9246') {
      FormManager.setValue('affiliate', '0070800');
    } else if (isicCd == '9247') {
      FormManager.setValue('affiliate', '0070900');
    } else if (isicCd == '9248') {
      FormManager.setValue('affiliate', '0071800');
    } else if (isicCd == '9250') {
      FormManager.setValue('affiliate', '0072100');
    } else if (isicCd == '9251') {
      FormManager.setValue('affiliate', '0072200');
    } else if (isicCd == '9252') {
      FormManager.setValue('affiliate', '0072300');
    } else if (isicCd == '9253') {
      FormManager.setValue('affiliate', '0072400');
    } else if (isicCd == '9254') {
      FormManager.setValue('affiliate', '0072600');
    } else if (isicCd == '9255') {
      FormManager.setValue('affiliate', '0073400');
    } else if (isicCd == '9260') {
      FormManager.setValue('affiliate', '0075600');
    } else if (isicCd == '9261') {
      FormManager.setValue('affiliate', '0075800');
    } else if (isicCd == '9262') {
      FormManager.setValue('affiliate', '0076000');
    } else if (isicCd == '9263') {
      FormManager.setValue('affiliate', '0076200');
    } else if (isicCd == '9264') {
      FormManager.setValue('affiliate', '0076300');
    } else if (isicCd == '9265') {
      FormManager.setValue('affiliate', '0076900');
    } else if (isicCd == '9267') {
      FormManager.setValue('affiliate', '0077100');
    } else if (isicCd == '9268') {
      FormManager.setValue('affiliate', '0077200');
    } else if (isicCd == '9269') {
      FormManager.setValue('affiliate', '0077300');
    } else if (isicCd == '9273') {
      FormManager.setValue('affiliate', '0077700');
    } else if (isicCd == '9274') {
      FormManager.setValue('affiliate', '0079800');
    } else if (isicCd == '9275') {
      FormManager.setValue('affiliate', '0080900');
    } else if (isicCd == '9276') {
      FormManager.setValue('affiliate', '0081100');
    } else {
      FormManager.setValue('affiliate', '');
    }
  } else {
    FormManager.setValue('affiliate', '');
  }

}
// CREATCMR-6587

/* Register US Javascripts */
dojo.addOnLoad(function() {
  console.log('adding US scripts...');
  GEOHandler.registerValidator(addInvoiceAddressLinesValidator, [ SysLoc.USA ], null, true);
  // GEOHandler.registerValidator(addCountyValidator, [ SysLoc.USA ], null,
  // true);
  GEOHandler.registerValidator(addCreateByModelValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addAddressRecordTypeValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addCtcObsoleteValidator, [ SysLoc.USA ], null, true);
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
  GEOHandler.registerValidator(addCompanyEnterpriseValidation, [ SysLoc.USA ], null, true);
  // ], null, true);
  GEOHandler.addAfterConfig(lockOrdBlk, [ SysLoc.USA ]);
  GEOHandler.registerValidator(orderBlockValidation, [ SysLoc.USA ], null, true);

  // CREATCMR-3298
  GEOHandler.addAfterConfig(checkSCCValidate, [ SysLoc.USA ]);
  GEOHandler.registerValidator(sccWarningShowAndHide, [ SysLoc.USA ], null, false);

  GEOHandler.addAddrFunction(hideKUKLA, [ SysLoc.USA ]);
  // CREATCMR-6375
  // GEOHandler.registerValidator(addKuklaValidator, [ SysLoc.USA ], null,
  // true);
  // CREATCMR-6255
  // GEOHandler.registerValidator(addDivStreetCountValidator, [ SysLoc.USA ],
  // null, true);

  GEOHandler.addAfterTemplateLoad(setClientTierValuesUS, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(setClientTierValuesUS, [ SysLoc.USA ]);
  // CREATCMR-5447
  GEOHandler.registerValidator(TaxTeamUpdateDataValidation, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(TaxTeamUpdateAddrValidation, [ SysLoc.USA ], null, true);
});