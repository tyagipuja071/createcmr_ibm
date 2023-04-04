/*
 * File: us_validations.js
 * Description:
 * Contains the specific validations and configuration adjustments for US (897)
 */

var _usSicmenHandler = null;
var _usIsuHandler = null;
var _usTaxcd1Handler = null;
var _usSicm = "";
var _kukla = "";
var _enterpriseHandler = null;
var _usRestrictToHandler = null;
var affiliateArray = {
  9001 : '0089800',
  9002 : '0084800',
  9003 : '0086100',
  9004 : '0085900',
  9005 : '0086000',
  9006 : '0085800',
  9007 : '0080200',
  9008 : '0085300',
  9009 : '0085600',
  9010 : '0080300',
  9011 : '0088300',
  9012 : '0086900',
  9013 : '0082200',
  9014 : '0082100',
  9015 : '0082700',
  9016 : '0085300',
  9017 : '0082800',
  9018 : '0082900',
  9019 : '0082600',
  9020 : '0083400',
  9021 : '0083100',
  9022 : '0083200',
  9023 : '0082500',
  9024 : '0083000',
  9025 : '0088600',
  9026 : '0088800',
  9027 : '0083300',
  9028 : '0083100',
  9029 : '0086400',
  9030 : '0081900',
  9031 : '0081000',
  9032 : '0087900',
  9033 : '0085500',
  9034 : '0084200',
  9035 : '0084300',
  9036 : '0084700',
  9037 : '0084400',
  9038 : '0076500',
  9040 : '0084900',
  9041 : '0085000',
  9042 : '0089300',
  9043 : '0089400',
  9044 : '0089000',
  9045 : '0089500',
  9046 : '0060100',
  9048 : '0062100',
  9049 : '0069300',
  9050 : '0066600',
  9051 : '0069600',
  9052 : '0074800',
  9053 : '0082300',
  9054 : '0080600',
  9055 : '0060600',
  9056 : '0080400',
  9057 : '0088400',
  9058 : '0088500',
  9060 : '0062300',
  9061 : '0062400',
  9063 : '0062600',
  9064 : '0062700',
  9065 : '0088700',
  9066 : '0062200',
  9067 : '0061300',
  9068 : '0061400',
  9069 : '0061500',
  9070 : '0085700',
  9071 : '0061800',
  9072 : '0060200',
  9073 : '0060300',
  9074 : '0060400',
  9075 : '0060500',
  9076 : '0069000',
  9077 : '0060700',
  9078 : '0060800',
  9079 : '0061100',
  9080 : '0075200',
  9081 : '0061600',
  9083 : '0062000',
  9084 : '0061900',
  9085 : '0080500',
  9086 : '0074000',
  9087 : '0061700',
  9088 : '0060900',
  9089 : '0061000',
  9093 : '0066000',
  9095 : '0065300',
  9096 : '0061200',
  9098 : '0066800',
  9099 : '0076800',
  9100 : '0067200',
  9102 : '0068600',
  9104 : '0068800',
  9105 : '0068900',
  9108 : '0069200',
  9109 : '0065000',
  9110 : '0063700',
  9111 : '0066800',
  9112 : '0064100',
  9113 : '0069700',
  9115 : '0064200',
  9117 : '0073100',
  9119 : '0073300',
  9120 : '0073500',
  9121 : '0064300',
  9122 : '0073700',
  9124 : '0073900',
  9125 : '0064400',
  9126 : '0064500',
  9127 : '0064600',
  9129 : '0070100',
  9130 : '0064700',
  9131 : '0070300',
  9132 : '0064800',
  9134 : '0071000',
  9135 : '0064900',
  9136 : '0071200',
  9137 : '0071300',
  9138 : '0071400',
  9139 : '0065400',
  9140 : '0065500',
  9141 : '0072000',
  9142 : '0082400',
  9147 : '0079700',
  9148 : '0078600',
  9151 : '0078900',
  9153 : '0078300',
  9155 : '0078400',
  9156 : '0078500',
  9157 : '0078700',
  9158 : '0078800',
  9159 : '0079100',
  9160 : '0062100',
  9162 : '0065600',
  9163 : '0065800',
  9164 : '0065900',
  9165 : '0080100',
  9166 : '0066200',
  9167 : '0066300',
  9168 : '0066500',
  9169 : '0066700',
  9170 : '0067400',
  9171 : '0067500',
  9172 : '0067600',
  9173 : '0067700',
  9174 : '0067800',
  9175 : '0068100',
  9176 : '0067900',
  9178 : '0068200',
  9179 : '0068300',
  9180 : '0068400',
  9181 : '0068500',
  9182 : '0069400',
  9183 : '0069500',
  9184 : '0086100',
  9185 : '0076500',
  9186 : '0076600',
  9187 : '0076700',
  9189 : '0075500',
  9191 : '0075700',
  9192 : '0071100',
  9193 : '0075900',
  9194 : '0070700',
  9195 : '0076100',
  9196 : '0071500',
  9197 : '0071600',
  9198 : '0076400',
  9199 : '0075100',
  9200 : '0076500',
  9201 : '0075000',
  9203 : '0085100',
  9204 : '0085100',
  9207 : '0084100',
  9208 : '0085100',
  9209 : '0085200',
  9210 : '0086200',
  9211 : '0086500',
  9213 : '0087100',
  9214 : '0087200',
  9215 : '0087300',
  9216 : '0086700',
  9218 : '0086100',
  9219 : '0086800',
  9222 : '0062800',
  9223 : '0062900',
  9224 : '0063000',
  9226 : '0063200',
  9227 : '0063300',
  9228 : '0063400',
  9230 : '0089700',
  9231 : '0089900',
  9232 : '0085200',
  9234 : '0075300',
  9235 : '0088900',
  9236 : '0089200',
  9237 : '0089600',
  9239 : '0072700',
  9240 : '0072900',
  9241 : '0073000',
  9242 : '0074300',
  9243 : '0088000',
  9244 : '0079900',
  9245 : '0085400',
  9246 : '0070800',
  9247 : '0070900',
  9248 : '0071800',
  9250 : '0072100',
  9251 : '0072200',
  9252 : '0072300',
  9253 : '0072400',
  9254 : '0072600',
  9255 : '0073400',
  9260 : '0075600',
  9261 : '0075800',
  9262 : '0076000',
  9263 : '0076200',
  9264 : '0076300',
  9265 : '0076900',
  9267 : '0077100',
  9268 : '0077200',
  9269 : '0077300',
  9273 : '0077700',
  9274 : '0079800',
  9275 : '0080900',
  9276 : '0081100'
}
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
  FormManager.addFormValidator((function() {
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
            return new ValidationResult(null, false, 'The request should contain exactly one Install At address and one optional Invoice To address.');
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
            && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "M" || clientTier == "V" || clientTier == "Z" || clientTier == "S" || clientTier == "N"
                || clientTier == "C" || clientTier == "0")) {
          return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid value from list.');
        } else if (reqType == 'U'
            && oldCtc != null
            && oldCtc != clientTier
            && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "M" || clientTier == "V" || clientTier == "Z" || clientTier == "S" || clientTier == "N"
                || clientTier == "C" || clientTier == "0")) {
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

var resetIsicFlag = -1;

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
  } else if (reqType == 'C' && role == 'Requester' && custGrp == '15' && custSubGrp == 'FSP POOL') {
    FormManager.setValue('isuCd', '28');
    FormManager.setValue('clientTier', '');
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
      if (FormManager.getActualValue('userRole').toUpperCase() == 'REQUESTER' && FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('custType') == '7') {
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
        //CreateCMR-8143
        //FormManager.readOnly('postCd');
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

          if (resetIsicFlag > 0) {
            FormManager.setValue('isicCd', _usSicm);
          } else {
            FormManager.setValue('isicCd', _pagemodel.isicCd);
            resetIsicFlag++;
          }

          // CREATCMR-6587
          setAffiliateNumber();
        }
      }
    });
  }
  _usSicmenHandler[0].onChange();

  if (reqType == 'U') {
    FormManager.readOnly('custType');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
  }

  if (_usIsuHandler == null && FormManager.getField('isuCd')) {
    _usIsuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValuesUS();
    });
  }
  // CREATCMR-6777
  if (_usTaxcd1Handler == null && FormManager.getField('taxCd1')) {
    _usTaxcd1Handler = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function(value) {
      setTaxcd1Status();
    });
  }

  if (_enterpriseHandler == null) {
    _enterpriseHandler = dojo.connect(FormManager.getField('enterprise'), 'onChange', function(value) {
      updateBOForEntp();
    });
  }
  // CREATCMR-6987
  if (_usRestrictToHandler == null && FormManager.getField('restrictTo')) {
    _usRestrictToHandler = dojo.connect(FormManager.getField('restrictTo'), 'onChange', function(value) {
      setMainName1ForKYN();
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
  } else if (scenario != 'FSP POOL') {
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

function setBPNameValuesUS() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // For Federal Strategic Partners
  if (FormManager.getActualValue('custGrp') == '15') {
    FormManager.setValue('bpName', 'IRMR');
    if (FormManager.getActualValue('userRole').toUpperCase() == 'REQUESTER') {
      FormManager.readOnly('bpName');
    }
    return;
  }
  FormManager.enable('bpName');
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

    if (landCntry == 'US') {
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
            $("#scc").val(sccValue);
          } else {
            $('#sccWarn').show();
          }

        } else {
          $('#sccWarn').show();
        }
      } else {
        $('#sccWarn').show();
      }
    } else {
      var ret1 = cmr.query('US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY_NON_US', {
        LAND_CNTRY : landCntry,
        N_CITY : city
      });

      var sccValue = '';

      if (ret1 && ret1.ret1 && ret1.ret1 != '') {
        sccValue = ret1.ret1;
        // CREATCMR-5447
        $("#addressTabSccInfo").html(sccValue);
        $("#scc").val(sccValue);
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

function checkSCCValidateForProcessor() {

  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Processor') {

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
            $("#scc").val(sccValue);
          } else {
            $('#sccWarn').show();
          }

        } else {
          $('#sccWarn').show();
        }
      } else {
        $('#sccWarn').show();
      }
    }

    FormManager.addValidator('scc', Validators.REQUIRED, [ 'SCC Code' ], 'MAIN_NAME_TAB');
  }
}

function addLatinCharValidatorUS() {
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Address' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
  FormManager.addValidator('city2', Validators.LATIN, [ 'District' ]);
  FormManager.addValidator('divn', Validators.LATIN, [ 'Division/Address Con\'t' ]);
  FormManager.addValidator('dept', Validators.LATIN, [ 'Department / Attn' ]);
  FormManager.addValidator('bldg', Validators.LATIN, [ 'Building' ]);
  FormManager.addValidator('floor', Validators.LATIN, [ 'Floor' ]);
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
        if (FormManager.getActualValue('addrType') != 'ZP01' && FormManager.getActualValue('addrType') != 'ZS01' && FormManager.getActualValue('addrType') != 'ZI01'
            || FormManager.getActualValue('bpAcctTyp') == 'E') {
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
  FormManager.addValidator('mainCustNm1', Validators.LATIN, [ 'Customer Name' ], 'MAIN_GENERAL_TAB');
  FormManager.addValidator('mainCustNm2', Validators.LATIN, [ 'Customer Name 2' ], 'MAIN_GENERAL_TAB');
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
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var fspEndUser = 'FSP END USER';
  var fspPool = 'FSP POOL';
  if (custSubGrp == fspEndUser || custSubGrp == fspPool) {
    return;
  }
  // This will override the affiliate value in UI if calculated via Automation
  // Element
  if (subIndustryCd.startsWith('Y') && (isicCd.startsWith('90') || isicCd.startsWith('91') || isicCd.startsWith('92'))) {
    FormManager.setValue('affiliate', affiliateArray[isicCd]);
  }
}
// CREATCMR-6587
// CREATCMR-6777
function setTaxcd1Status() {
  var taxCd1 = FormManager.getActualValue('taxCd1');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'C' && (role == 'REQUESTER' || role == 'PROCESSOR')) {
    if (taxCd1.indexOf("000") != -1) {
      FormManager.setValue('specialTaxCd', 'X');
    } else if (FormManager.getActualValue('specialTaxCd') != '') {
      FormManager.setValue('specialTaxCd', '');
    }
  }

}
// CREATCMR-6987
function setMainName1ForKYN() {
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var restrictTo = FormManager.getActualValue('restrictTo');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'C') {
    if ((custGrp == '3' && custSubGrp == 'KYN') || (custGrp == '14' && custSubGrp == 'BYMODEL' && restrictTo == 'KYN')) {
      FormManager.setValue('mainCustNm1', 'KYNDRYL INC');
      FormManager.setValue('mainCustNm2', '');
      // CREATCMR-7173
      FormManager.setValue('isuCd', '5K');
    }
    if (custGrp == '3' && custSubGrp == 'KYN') {
      FormManager.setValue('custType', '1');
      FormManager.readOnly('custType');
    }
  }

}
function addressQuotationValidator() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Address' ]);
  FormManager.addValidator('divn', Validators.NO_QUOTATION, [ 'Division/Address Con\'t' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Department / Attn' ]);
  FormManager.addValidator('city2', Validators.NO_QUOTATION, [ 'District' ]);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'Building' ]);
  FormManager.addValidator('floor', Validators.NO_QUOTATION, [ 'Floor' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Zip Code' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
  FormManager.addValidator('custFax', Validators.NO_QUOTATION, [ 'FAX' ]);
  FormManager.addValidator('transportZone', Validators.NO_QUOTATION, [ 'Transport Zone' ]);
  FormManager.addValidator('mainCustNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('mainCustNm2', Validators.NO_QUOTATION, [ 'Customer Name 2' ]);
}

// CREATCMR-7213
function federalIsicCheck() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custGrp = FormManager.getActualValue('custGrp');
        var subIndustryCd = FormManager.getActualValue('subIndustryCd');
        var fedIsic = [ '9', '10', '11', '14' ];
        if (reqType == 'C' && !fedIsic.includes(custGrp) && custGrp != '15' && custGrp != '5' && subIndustryCd.startsWith('Y')) {
          genericMsg = 'Federal ISIC cannot be used with Non-Federal scenarios.';
          return new ValidationResult(null, false, genericMsg);
        }
        if (reqType == 'C' && custGrp == '15' && !subIndustryCd.startsWith('Y')) {
          genericMsg = 'Only Federal ISIC can be used with Federal Strategic Partner scenarios.';
          return new ValidationResult(null, false, genericMsg);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

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
  //GEOHandler.registerValidator(addCompanyEnterpriseValidation, [ SysLoc.USA ], null, true);
  // ], null, true);
  GEOHandler.addAfterConfig(lockOrdBlk, [ SysLoc.USA ]);
  GEOHandler.registerValidator(orderBlockValidation, [ SysLoc.USA ], null, true);

  // CREATCMR-3298
  GEOHandler.addAfterConfig(checkSCCValidate, [ SysLoc.USA ]);
  GEOHandler.registerValidator(sccWarningShowAndHide, [ SysLoc.USA ], null, false);
  GEOHandler.registerValidator(checkSCCValidateForProcessor, [ SysLoc.USA ], null, false);
  GEOHandler.addAddrFunction(addLatinCharValidatorUS, [ SysLoc.USA ]);

  GEOHandler.addAddrFunction(hideKUKLA, [ SysLoc.USA ]);
  // CREATCMR-6375
  // GEOHandler.registerValidator(addKuklaValidator, [ SysLoc.USA ], null,
  // true);
  // CREATCMR-6255
  // GEOHandler.registerValidator(addDivStreetCountValidator, [ SysLoc.USA ],
  // null, true);

  GEOHandler.addAfterTemplateLoad(setClientTierValuesUS, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(setBPNameValuesUS, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(setClientTierValuesUS, [ SysLoc.USA ]);
  // CREATCMR-5447
  GEOHandler.registerValidator(TaxTeamUpdateDataValidation, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(TaxTeamUpdateAddrValidation, [ SysLoc.USA ], null, true);
  // CREATCMR-6987
  GEOHandler.addAfterTemplateLoad(setMainName1ForKYN, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(setMainName1ForKYN, [ SysLoc.USA ]);
  // CREATCMR-788
  GEOHandler.addAddrFunction(addressQuotationValidator, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(addressQuotationValidator, [ SysLoc.USA ]);
  // CREATCMR-7213
  GEOHandler.registerValidator(federalIsicCheck, [ SysLoc.USA ], null, true);
});
