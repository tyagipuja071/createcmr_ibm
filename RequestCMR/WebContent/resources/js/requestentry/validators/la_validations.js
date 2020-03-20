/*
 * File: la_validations.js Description: Contains the specific javascript,
 * validations and configuration adjustments for Latin America
 */
var COV_2018 = false;
var VAT_LENGTH = 14;

var SSA_COUNTRIES = [ SysLoc.MEXICO, SysLoc.ARGENTINA, SysLoc.BOLIVIA, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA, SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR, SysLoc.GUATEMALA, SysLoc.HONDURAS,
    SysLoc.NICARAGUA, SysLoc.PANAMA, SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY, SysLoc.VENEZUELA ];

var SSAMX_COUNTRIES = [ SysLoc.MEXICO, SysLoc.ARGENTINA, SysLoc.BOLIVIA, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA, SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR, SysLoc.GUATEMALA,
    SysLoc.HONDURAS, SysLoc.NICARAGUA, SysLoc.PANAMA, SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY, SysLoc.VENEZUELA ];

var LA_COUNTRIES = [ SysLoc.BRAZIL, SysLoc.MEXICO, SysLoc.ARGENTINA, SysLoc.BOLIVIA, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA, SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR, SysLoc.GUATEMALA,
    SysLoc.HONDURAS, SysLoc.NICARAGUA, SysLoc.PANAMA, SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY, SysLoc.VENEZUELA ];

var LAHandler = {
  isLAIssuingCountry : function() {
    var sysLocCd = FormManager.getActualValue('cmrIssuingCntry');
    if (LA_COUNTRIES.indexOf(sysLocCd) > -1) {
      return true;
    } else {
      return false;
    }
  },

  isSSAIssuingCountry : function() {
    var sysLocCd = FormManager.getActualValue('cmrIssuingCntry');
    if (SSA_COUNTRIES.indexOf(sysLocCd) > -1) {
      return true;
    } else {
      return false;
    }
  },
};

var LAReactivate = {
  isLAReactivateFlag : function() {
    if (FormManager.getActualValue('ordBlk') == '93' && FormManager.getActualValue('reqType') == 'U') {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      var laReactiveEnable = cmr.query('LA_REACTIVATE_COUNTRY', {
        CNTRY_CD : cntry,
      });
      var suppCntryFlag = laReactiveEnable.ret1.includes("1");
      if (suppCntryFlag) {
        return true;
      } else {
        return false;
      }
    }

  }
};

/** Mukesh:Story 1067140: START Sucursal/Collection Branch Office */
function doSucursalCollBOSearch() {
  var isDisabled = dojo.byId('collBoId').readOnly;
  if (!isDisabled) {
    cmr.sucursalCollBOsearchon = true;
    cmr.showModal('sucursalCollBranchOffModel');
  }
}

function closeSucursalcollBOSearch() {
  cmr.sucursalCollBOsearchon = false;
  cmr.hideModal('sucursalCollBranchOffModel');
}

function clearSucursalcollBO() {
  var isDisabled = dojo.byId('collBoId').readOnly;
  if (!isDisabled) {
    FormManager.clearValue('collBoId');
    dojo.byId('collBODesc').innerHTML = '(none selected)';
  }
}

function searchSucursalcollBO() {
  var obj = dojo.formToObject('frmCMRSucursalCollBO');
  var crmIssCntForCBO = FormManager.getActualValue('cmrIssuingCntry');
  var url = cmr.CONTEXT_ROOT + '/sucursalcollbranchoff/search.json';
  var params = 'collBOIDSearch=' + obj.collBOIDSearch;
  params += '&collBODescSearch=' + obj.collBODescSearch;
  params += '&issuingCntrySearch=' + crmIssCntForCBO;
  CmrGrid.refresh('sucursalcollBOSearchGrid', url, params);
  CmrGrid.correct('sucursalcollBOSearchGrid');
}

function clearSucursalcollBOSearch() {
  FormManager.clearValue('collBOIDSearch');
  FormManager.clearValue('collBODescSearch');
  searchSucursalcollBO();
}

function sucursalCollBranchOffModel_onLoad() {
  FormManager.setValue('collBOIDSearch', FormManager.getActualValue('collBoId'));
  searchSucursalcollBO();
}

function selectSucursalCollBOFormatter(value, index) {
  var rowData = this.grid.getItem(index);
  var collBoId = rowData.collBOIDSearch;
  var collBODesc = rowData.collBODescSearch;
  return '<a href="javascript: selectSucursalCollBO(\'' + collBoId + '\',\'' + collBODesc + '\')">Select</a>';
}

function selectSucursalCollBO(collBoId, collBODesc) {
  FormManager.setValue('collBoId', collBoId);
  dojo.byId('collBODesc').innerHTML = collBODesc;
  if ('OTH' == collBoId) {
    cmr.showAlert("Please specify Collection Branch Office in the Comment field.", "CBO");
    closeSucursalcollBOSearch();
  } else {
    closeSucursalcollBOSearch();
  }
}
/** Mukesh:Story 1067140: END Sucursal/Collection Branch Office */

/*
 * Story 1068203: LA: Coverage fields need to be accustomed to LA. Author:
 * Dennis T Natad Purpose: Function for displayedValue retrieval in the case of
 * select objects and similar objects.
 */
function getDescription(fieldId) {
  var field = dijit.byId(fieldId);
  return field.displayedValue;
}

var _mrcCdHandler = null;
var _custNameHandler = null;
var _salesBranchOffHandler = null;
var _mrcCdHandler = null;
var _customerTypeHandler = null;
var _subindustryHandler = null;
var wwSubindustries = [ 'A9', 'AA', 'AW', 'B9', 'BB', 'BD', 'BG', 'BI', 'BW', 'CB', 'CC', 'CD', 'CW', 'D9', 'DI', 'DJ', 'DK', 'DL', 'DW', 'E9', 'EA', 'ER', 'EW', 'F9', 'FA', 'FW', 'GA', 'GE', 'GW',
    'H9', 'HA', 'HB', 'HE', 'HW', 'J9', 'JA', 'JB', 'JC', 'JD', 'JW', 'K9', 'KA', 'KB', 'KC', 'KD', 'KE', 'KF', 'KW', 'KZ', 'L9', 'LA', 'LB', 'LW', 'MC', 'ME', 'MH', 'MI', 'MJ', 'MK', 'ML', 'MW',
    'N9', 'NA', 'NB', 'NI', 'NW', 'NZ', 'P9', 'PC', 'PO', 'PW', 'R9', 'RA', 'RC', 'RD', 'RE', 'RF', 'RG', 'RH', 'RR', 'RW', 'S9', 'SB', 'SW', 'T9', 'TA', 'TC', 'TD', 'TE', 'TF', 'TG', 'TH', 'TJ',
    'TL', 'TM', 'TO', 'TW', 'U9', 'UA', 'UB', 'UD', 'UE', 'UW', 'V9', 'VA', 'VB', 'VC', 'VW', 'WN', 'WO', 'WP', 'WQ', 'WW', 'XF', 'XW', 'Y9', 'YA', 'YE', 'YW', 'ZA', 'ZB', 'ZC', 'ZD', 'ZE', 'ZF',
    'ZZ' ];

var contains = function(needle) {
  var findNaN = needle !== needle;
  var indexOf;
  if (!findNaN && typeof Array.prototype.indexOf === 'function') {
    indexOf = Array.prototype.indexOf;
  } else {
    indexOf = function(needle) {
      var i = -1, index = -1;
      for (i = 0; i < this.length; i++) {
        var item = this[i];
        if ((findNaN && item !== item) || item === needle) {
          index = i;
          break;
        }
      }
      return index;
    };
  }
  return indexOf.call(this, needle) > -1;
};

function autoSetMrcIsu() {
  var sboCd = FormManager.getActualValue('salesBusOffCd');
  console.log(">>> process SBO >> " + sboCd);
  FormManager.setValue('sboMrc', sboCd);
  var mrcCd = getDescription('sboMrc');
  console.log(">>> process MRC >> " + mrcCd);
  FormManager.setValue('mrcCd', mrcCd);
  FormManager.setValue('mrcIsu', FormManager.getActualValue('mrcCd'));
  var mrcDesc = getDescription('mrcIsu');
  console.log(">>> process ISU >> " + mrcDesc);

  if (mrcDesc != "") {
    FormManager.setValue('isuCd', mrcDesc);
  }
}

function autoSetMrcIsuCov2018() {
  // var sboCd = FormManager.getActualValue('salesBusOffCd');
  // console.log(">>> process SBO >> " + sboCd);
  // FormManager.setValue('sboMrc', sboCd);
  // var mrcCd = getDescription('sboMrc');
  // console.log(">>> process MRC >> " + mrcCd);
  // FormManager.setValue('mrcCd', mrcCd);
  // FormManager.setValue('mrcIsu', FormManager.getActualValue('mrcCd'));
  var mrcDesc = getDescription('mrcIsu');
  console.log(">>> process ISU >> " + mrcDesc);

  if (mrcDesc != "") {
    FormManager.setValue('isuCd', mrcDesc);
  }
}

// dtn defect 1435522
function autoSetDataCrosTypSubTypeSSAMX() {

}

// Story :1278109- By Mukesh Kumar
function autoSetSBOAndSalesTMNo() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custType = FormManager.getActualValue('custType');
  console.log(">>> Process _custSubGrp >> " + _custSubGrp);
  console.log(">>> Process _custType >> " + _custType);
  if (FormManager.getActualValue('cmrIssuingCntry') == '631' && FormManager.getActualValue('reqType') == 'C') {
    if (_custSubGrp != 'undefined' && _custSubGrp == 'CROSS') {
      FormManager.enable('repTeamMemberNo');
      FormManager.enable('salesBusOffCd');
      if (_custType == 'INTER') {
        FormManager.setValue('repTeamMemberNo', '010200');
        FormManager.setValue('salesBusOffCd', '010');
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('salesBusOffCd');

        FormManager.setValue('crosTyp', '9');
        console.log(">>> Process repTeamMemberNo >> " + FormManager.getActualValue('repTeamMemberNo'));
        console.log(">>> Process salesBusOffCd >> " + FormManager.getActualValue('salesBusOffCd'));
      }
      if (_custType == 'BUSPR') {
        FormManager.setValue('salesBusOffCd', '606');
        FormManager.readOnly('salesBusOffCd');
        console.log(">>> Process salesBusOffCd >> " + FormManager.getActualValue('salesBusOffCd'));

      }
      /* #1355290 */
      if (_custType == 'BUSPR' || _custType == 'CC3CC' || _custType == 'COMME' || _custType == 'CROSB' || _custType == 'INTER' || _custType == 'LEASI' || _custType == 'SOFTL' || _custType == 'IBMEM'
          || _custType == 'BLUEM' || _custType == 'PRIPE' || _custType == '5PRIP' || _custType == '5COMP') {
        var field = FormManager.getField('govType');
        FormManager.limitDropdownValues(field, [ 'OU' ]);
        field.set('value', 'OU');
        FormManager.readOnly('govType');
        FormManager.resetValidations('govType');
        if (_custType != 'INTER') {
          FormManager.setValue('crosTyp', '0');
        }
        console.log(">>> Process govType >> " + FormManager.getActualValue('govType'));
      }

      if (_custType == 'GOVDI' || _custType == 'GOVIN') {
        console.log("Government FormManager.getActualValue('govType')===" + FormManager.getActualValue('govType'));
        var field = FormManager.getField('govType');
        FormManager.limitDropdownValues(field, [ 'CM', 'PF', 'PM', 'PE' ]);
        if (FormManager.getActualValue('govType') == '') {
          field.set('value', 'CM');
        }
        var lblGovernment = FormManager.getLabel('Government');
        FormManager.addValidator('govType', Validators.REQUIRED, [ lblGovernment ], 'MAIN_CUST_TAB');
        FormManager.enable('govType');
        FormManager.setValue('crosTyp', '0');
        console.log(">>> Government Process govType >> " + FormManager.getActualValue('govType'));
      }
    }
  }
}

/* #1356017 */
function autoSetIcmsFieldForCrossBR() {
  console.log('autoSetIcmsFieldForCrossBR : processing. . .');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custType = FormManager.getActualValue('custType');
  var _reqType = FormManager.getActualValue('reqType');
  var _cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (_cmrCntry == '631' && _reqType == 'C') {
    if ((_custType == '5PRIP' || _custType == '5COMP' || _custType == 'PRIPE' || _custType == 'IBMEM' || _custType == 'BLUEM') && _custSubGrp == 'CROSS') {
      console.log('autoSetIcmsFieldForCrossBR : custType is ' + _custType);
      FormManager.removeValidator('icmsInd', Validators.REQUIRED);
      FormManager.setValue('icmsInd', '1');
      FormManager.readOnly('icmsInd');
    }
  }

  /*
   * // Dennis : Commenting as this is causing some issues on the CROS end. //
   * Mukesh :Story 1461414: CROS fields Customer Sub-type 1 & Customer Sub-type //
   * 2 to be defaulted based on the Scenario sub-type if (_cmrCntry == '781' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType); if (_custType == 'INTER') { FormManager.setValue('govType',
   * '9'); } if (_custType == 'COMME' || _custType == 'BUSPR' || _custType ==
   * 'GOVDI' || _custType == 'GOVIN' || _custType == 'ALTAR' || _custType ==
   * 'SOFTL') { FormManager.setValue('govType', '0'); } } // SSA For 613 if
   * (_cmrCntry == '613' && _reqType == 'C') { console.log('Auto set govType
   * based on _custType=' + _custType); if (_custType == 'INTOU' || _custType ==
   * 'INTUS') { FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 629 if (_cmrCntry == '629' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType); if (_custType == 'INTER') {
   * FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 655 if (_cmrCntry == '655' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType + ' for _cmrCntry=' + _cmrCntry);
   * if (_custType == 'INTER') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 661 if (_cmrCntry == '661' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INIBM' ||
   * _custType == 'INTOU') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 663 if (_cmrCntry == '663' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INTER') {
   * FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 681 if (_cmrCntry == '681' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType + ' for _cmrCntry=' + _cmrCntry);
   * if (_custType == 'INIBM') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 683 if (_cmrCntry == '683' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INTER') {
   * FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 731 if (_cmrCntry == '731' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType + ' for _cmrCntry=' + _cmrCntry);
   * if (_custType == 'INIBM') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 735 if (_cmrCntry == '735' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INIBM') {
   * FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 799 if (_cmrCntry == '799' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType + ' for _cmrCntry=' + _cmrCntry);
   * if (_custType == 'INIBM') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 811 if (_cmrCntry == '811' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INIBM') {
   * FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 813 if (_cmrCntry == '813' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType + ' for _cmrCntry=' + _cmrCntry);
   * if (_custType == 'INTER') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 815 if (_cmrCntry == '815' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INTOU' ||
   * _custType == 'INIBM') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 829 if (_cmrCntry == '829' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INIBM') {
   * FormManager.setValue('govType', '9'); } if (_custType == 'COMME' ||
   * _custType == 'BUSPR' || _custType == 'GOVDI' || _custType == 'GOVIN' ||
   * _custType == 'SOFTL' || _custType == 'SAASP' || _custType == 'IBMEM' ||
   * _custType == 'PRIPE') { FormManager.setValue('govType', '0'); } } // For
   * 869 if (_cmrCntry == '869' && _reqType == 'C') { console.log('Auto set
   * govType based on _custType=' + _custType + ' for _cmrCntry=' + _cmrCntry);
   * if (_custType == 'INIBM') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } } // For 871 if (_cmrCntry == '871' &&
   * _reqType == 'C') { console.log('Auto set govType based on _custType=' +
   * _custType + ' for _cmrCntry=' + _cmrCntry); if (_custType == 'INIBM' ||
   * _custType == 'INTOU') { FormManager.setValue('govType', '9'); } if
   * (_custType == 'COMME' || _custType == 'BUSPR' || _custType == 'GOVDI' ||
   * _custType == 'GOVIN' || _custType == 'SOFTL' || _custType == 'SAASP' ||
   * _custType == 'IBMEM' || _custType == 'PRIPE') {
   * FormManager.setValue('govType', '0'); } }
   */
}

/* #1351894 */
function resetIbmBankNumber() {
  var _custType = FormManager.getActualValue('custType');
  var _country = FormManager.getActualValue('cmrIssuingCntry');
  if (_country == '631') {
    if (_custType == '5PRIP' || _custType == '5COMP' || _custType == 'PRIPE' || _custType == 'IBMEM' || _custType == 'BLUEM') {
      if (typeof (_pagemodel) != 'undefined') {
        if (_pagemodel.userRole == 'Processor') {
          FormManager.removeValidator('ibmBankNumber', Validators.REQUIRED);
        }
      }
    }
  }
}

function afterConfigForLA() {
  var _reqType = FormManager.getActualValue('reqType');
  // var _country = FormManager.getActualValue('cmrIssuingCntry');

  if (dojo.byId('isuCd')) {
    // FormManager.disable('isuCd');
    FormManager.readOnly('isuCd');
  }
  if (dojo.byId('collBoId' && _reqType != 'U')) {
    dojo.byId('collBoId').readOnly = true;
  }
  // DENNIS: Original code allows us to auto select MRC when the SBO is
  // selected
  // if (_salesBranchOffHandler == null && !COV_2018) {
  // _salesBranchOffHandler =
  // dojo.connect(FormManager.getField('salesBusOffCd'), 'onChange',
  // function(value) {
  // autoSetMrcIsu();
  // });
  // }

  // DENNIS: This is new coverage handler where auto selecting ISU is done
  // through MRC
  if (_mrcCdHandler == null) {
    _mrcCdHandler = dojo.connect(FormManager.getField('mrcIsu'), 'onChange', function(value) {
      autoSetMrcIsuCov2018();
    });
  }

  if (_mrcCdHandler && _mrcCdHandler[0]) {
    _mrcCdHandler[0].onChange();
  }

  // Story :1278109- By Mukesh Kumar
  if (_customerTypeHandler == null) {
    window.setTimeout(autoSetSBOAndSalesTMNo, 6000);
    window.setTimeout(autoSetIcmsFieldForCrossBR, 6000);
    _customerTypeHandler = dojo.connect(FormManager.getField('custType'), 'onChange', function(value) {
      autoSetSBOAndSalesTMNo();
      filterAddressesGridForCrossBR();
      autoSetIcmsFieldForCrossBR();
      resetIbmBankNumber();
      autoSetFieldsForCustScenariosSSAMX();
      setCrosTypSubTypSSAMXOnSecnarios();
      setAbbrevNameRequiredForProcessors();
    });
  }

  if (_customerTypeHandler && _customerTypeHandler[0]) {
    _customerTypeHandler[0].onChange();
  }

  if (_custNameHandler == null) {
    _custNameHandler = dojo.connect(FormManager.getField('mainCustNm1'), 'onChange', function(value) {
      autoSetAbbrevNameForBrazil();
    });
  }

  // if(_subindustryHandler == null){
  // _subindustryHandler = dojo.connect(FormManager.getField('custType'),
  // 'onChange', function(value) {
  // if(!contains.call(wwSubindustries, FormManager.getActualValue(''))){
  //        
  // }
  // });
  // }
  //  
  // if (_subindustryHandler && _subindustryHandler[0]){
  // _subindustryHandler[0].onChange();
  // }
  checkAndRefillCollBoIdDescription();
  resetPhoneValidationForSSA();

  // if (_country == '815') {
  // resetMrcCdValidation();
  // }

  // var _reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('cmrIssuingCntry') == '683' && _pagemodel.userRole.toUpperCase() == 'REQUESTER') {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'PostalCode' ], '');
  }

  if (FormManager.getActualValue('ordBlk') == '93' && _reqType == 'U') {
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var laReactiveEnable = cmr.query('LA_REACTIVATE_COUNTRY', {
      CNTRY_CD : cntry,
    });
    var suppCntryFlag = laReactiveEnable.ret1.includes("1");
    if (suppCntryFlag) {
      makeFieldManadatoryLAReactivate();
    }
  }
}

function resetMrcCdValidation() {
  console.log('Inside resetMrcCdValidation...');
  var lblMrcCd = FormManager.getLabel('MrcCd');
  if (_pagemodel.userRole.toUpperCase() == 'PROCESSOR') {
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ lblSalesBusOff ], 'MAIN_IBM_TAB');
  }
}

function resetPhoneValidationForSSA() {
  console.log('resetPhoneValidationForSSA : processing . . . ');
  var country = FormManager.getActualValue('cmrIssuingCntry');
  if (SSAMX_COUNTRIES.indexOf(country) > -1) {
    if (country != '815') { // except PERU
      console.log('resetPhoneValidationForSSA : not -> PERU');
      if (FormManager.GETFIELD_VALIDATIONS['contactPhone'].indexOf(Validators.DIGIT) > -1) {
        FormManager.removeValidator('contactPhone', Validators.DIGIT);
        FormManager.addValidator('contactPhone', Validators.DIGIT_OR_DOT, [ 'Phone Number' ]);
      }
    }
  }
}

function checkAndRefillCollBoIdDescription() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var desc = dojo.byId('collBODesc').innerHTML;
  var fieldValue = FormManager.getActualValue('collBoId');
  if (fieldValue != '' && desc.trim() == '(none selected)') {
    var params = {
      BO_ID : fieldValue,
      CNTRY : cntry
    };
    var collBoRecord = cmr.query('GET_SUCURSALBO_DESC', params);
    dojo.byId('collBODesc').innerHTML = collBoRecord.ret1;
  }
}

function autoSetFieldsForCustScenariosSSAMX() {
  console.log('autoSetFieldsForCrossScenario : processing. . .');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _reqType = FormManager.getActualValue('reqType');
  var _cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var _custType = FormManager.getActualValue('custType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var internalFlag = false;
  var requesterFlag = false;
  if (SSAMX_COUNTRIES.indexOf(_cmrCntry) > -1 && _reqType == 'C') {
    console.log("autoSetFieldsForCrossScenario : current country belongs to SSA_MX. . .");
    console.log("autoSetFieldsForCrossScenario : current customer type : " + _custType);
    var isicStore = null;
    var isicLoadInterval = null;
    var currentIsicCd = '';
    if (_custSubGrp == 'CROSS') {
      if (_custType != '5PRIP' && _custType != '5COMP') {
        FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
        FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
        if (role == 'PROCESSOR') {
          FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'SalRepNameNo' ], 'MAIN_IBM_TAB');
        }
      } else {
        FormManager.resetValidations('isicCd');
        FormManager.resetValidations('subIndustryCd');
        FormManager.resetValidations('repTeamMemberNo');
      }
      if (_custType == 'PRIPE') {
        FormManager.setValue('subIndustryCd', 'WQ');
        FormManager.readOnly('subIndustryCd');
        /* wait isic field to load before setting */
        isicLoadInterval = setInterval(function() {
          isicStore = FormManager.getField('isicCd').store;
          if ((isicStore != undefined && isicStore._arrayOfAllItems != undefined) && isicStore._arrayOfAllItems.length > 0) {
            currentIsicCd = FormManager.getActualValue('isicCd');
            if (currentIsicCd != '9500') {
              FormManager.setValue('isicCd', '9500');
              FormManager.readOnly('isicCd');
            }
            currentIsicCd = FormManager.getActualValue('isicCd');
            if (currentIsicCd == '9500') {
              clearInterval(isicLoadInterval);
            }
          } else {
            console.log('autoSetFieldsForCrossScenario ; isic codes not yet loaded');
          }
        }, 500);
      } else if (_custType == 'INTER' || _custType == 'INTUS' || _custType == 'INTEQ' || _custType == 'INGBM' || _custType == 'INTPR' || _custType == 'INTOU' || _custType == 'INIBM') {
        FormManager.setValue('subIndustryCd', 'ZF');
        FormManager.readOnly('subIndustryCd');
        /* wait isic field to load before setting */
        isicLoadInterval = setInterval(function() {
          isicStore = FormManager.getField('isicCd').store;
          if ((isicStore != undefined && isicStore._arrayOfAllItems != undefined) && isicStore._arrayOfAllItems.length > 0) {
            currentIsicCd = FormManager.getActualValue('isicCd');
            if (currentIsicCd != '0000') {
              FormManager.setValue('isicCd', '0000');
              FormManager.readOnly('isicCd');
            }
            currentIsicCd = FormManager.getActualValue('isicCd');
            if (currentIsicCd == '0000') {
              clearInterval(isicLoadInterval);
            }
          } else {
            console.log('autoSetFieldsForCrossScenario ; isic codes not yet loaded');
          }
        }, 500);
        FormManager.setValue(FormManager.getField('salesBusOffCd'), '9A9');
        FormManager.readOnly('salesBusOffCd');
        FormManager.setValue(FormManager.getField('repTeamMemberNo'), '999999');
        FormManager.readOnly('repTeamMemberNo');
        FormManager.setValue(FormManager.getField('collectorNameNo'), '999999');
        FormManager.readOnly('collectorNameNo');
        internalFlag = true;
      }

      if (_pagemodel.userRole.toUpperCase() == 'REQUESTER') {
        requesterFlag = true;
        FormManager.readOnly('collectorNameNo');
      } else {
        FormManager.enable('collectorNameNo');
        requesterFlag = false;
      }
      if (!internalFlag && requesterFlag) {
        if (_cmrCntry == SysLoc.PERU) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'LVLKN7');
        } else if (_cmrCntry == SysLoc.COLOMBIA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'CZ9000');
        } else if (_cmrCntry == SysLoc.BOLIVIA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'LVLKN7');
        } else if (_cmrCntry == SysLoc.ARGENTINA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'HPGU09');
        } else if (_cmrCntry == SysLoc.ECUADOR) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), '111111');
        } else if (_cmrCntry == SysLoc.URUGUAY) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), '006800');
        } else if (_cmrCntry == SysLoc.VENEZUELA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), '000001');
        } else if (_cmrCntry == SysLoc.COSTA_RICA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.DOMINICAN_REPUBLIC) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.PANAMA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.HONDURAS) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.GUATEMALA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.NICARAGUA) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.EL_SALVADOR) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'V72347');
        } else if (_cmrCntry == SysLoc.PARAGUAY) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), '007327');
        } else if (_cmrCntry == SysLoc.CHILE) {
          FormManager.setValue(FormManager.getField('collectorNameNo'), 'HPGU02');
          FormManager.enable('collectorNameNo');
        }
      }
    }

    if (_custGrp == "LOCAL" && _custSubGrp != '') {
      var _collBoCd = FormManager.getActualValue('collBoId');
      var _collBoDesc = dojo.byId('collBODesc').innerHTML;
      /* #1164438 */
      if (_cmrCntry == SysLoc.URUGUAY) {
        if (_collBoCd == '508' && _collBoDesc != 'DEFAULT') {
          dojo.byId('collBODesc').innerHTML = 'DEFAULT';
        }
      } else if (_cmrCntry == SysLoc.VENEZUELA) {
        if (_collBoCd == '001' && _collBoDesc != 'CUENTAS NUEVAS') {
          dojo.byId('collBODesc').innerHTML = 'CUENTAS NUEVAS';
        }
      } else if (_cmrCntry == SysLoc.CHILE) {
        if (_collBoCd == '740' && _collBoDesc != 'BRANCH OFFICE') {
          dojo.byId('collBODesc').innerHTML = 'BRANCH OFFICE';
        }
      } else if (_cmrCntry == SysLoc.ARGENTINA) {
        if (_collBoCd == '999' && _collBoDesc != 'INTERNALS/DEMO') {
          dojo.byId('collBODesc').innerHTML = 'INTERNALS/DEMO';
        } else if (_collBoCd == '457' && _collBoDesc != 'DEFAULT') {
          dojo.byId('collBODesc').innerHTML = 'DEFAULT';
        }
      } else if (_cmrCntry == SysLoc.ECUADOR) {
        if (_collBoCd == '009' && _collBoDesc != 'CLIENTE INTERNO') {
          dojo.byId('collBODesc').innerHTML = 'CLIENTE INTERNO';
        }
      } else if (_cmrCntry == SysLoc.MEXICO) {
        if (_collBoCd == '103' && _collBoDesc != 'MA EUGENIA PEREZ LEON') {
          dojo.byId('collBODesc').innerHTML = 'MA EUGENIA PEREZ LEON';
        }
      }
    }
  }
}

/*
 * function to validate if the selected subindustry is for WW
 */
function addSubindustryValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log("Running addSubindustryValidator");
        var custTypeFlag = false;
        var _custType = FormManager.getActualValue('custType');
        if (_custType == '5COMP' || _custType == '5PRIP' || _custType == 'PRIPE' || _custType == 'IBMEM') {
          custTypeFlag = true;
        }
        if (!contains.call(wwSubindustries, FormManager.getActualValue('subIndustryCd')) && !custTypeFlag) {
          console.log("The Subindustry selected is specific for US. Please select another subindustry");
          return new ValidationResult(FormManager.getField('subIndustryCd'), false, 'The Subindustry selected is specific for US. Please select another subindustry');
        } else {
          console.log("Valid WW Subindustry is selected");
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/**
 * Validator for only a single Tax Info record for the request
 */
function addTaxInfoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxInfoReqId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var qParams = {
          REQ_ID : taxInfoReqId,
        };
        var record = cmr.query('COUNTTAXINFORECORDS', qParams);
        var taxInfoReccount = record.ret1;
        if (Number(taxInfoReccount == 0) && reqType == 'C') {
          return new ValidationResult(null, false, 'At least one tax info record must be defined');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_TAXINFO_TAB', 'frmCMR');
}

/**
 * Adds the validator for Tax Number for Argentina only dd-dddddddd-d can be
 * specified, where d is any valid digit.
 */
function addTaxNumberValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var value = FormManager.getActualValue('taxNum');
        var lbl = FormManager.getLabel('TaxNum');
        if (value && value.length > 0 && !value.match(/^\d{2}-\d{8}-\d{1}$/)) {
          return new ValidationResult({
            id : 'taxNum',
            type : 'text',
            name : 'taxNum'
          }, false, 'The value for ' + lbl + ' is invalid. For Argentina only dd-dddddddd-d format is allowed, where d is any valid digit.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_TAXINFO_TAB', 'frmCMR_taxInfoModal');
}

/**
 * Will add tax code one format/pattern validator on address modal form during
 * onLoad.
 * 
 * @return {ValidationResult}
 */
function addTaxCode1ValidatorInAddressModalForBrazil() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxCd1 = FormManager.getActualValue('taxCd1');
        var lbl1 = FormManager.getLabel('LocalTax1');
        if (taxCd1 && taxCd1.length > 0 && !taxCd1.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'The value for ' + lbl1 + ' is invalid. Only digits and alphabet combination is allowed');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * Will add tax code two format/pattern validator on address modal form during
 * onLoad.
 * 
 * @return {ValidationResult}
 */
function addTaxCode2ValidatorInAddressModalForBrazil() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxCd2 = FormManager.getActualValue('taxCd2');
        var lbl2 = FormManager.getLabel('LocalTax2');
        if (taxCd2 && taxCd2.length > 0 && !taxCd2.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult({
            id : 'taxCd2',
            type : 'text',
            name : 'taxCd2'
          }, false, 'The value for ' + lbl2 + ' is invalid. Only digits and alphabet combination is allowed');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addVatValidatorForOtherLACountries() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var value = FormManager.getActualValue('vat');
        var lbl = FormManager.getLabel('VAT');
        if (value && value.length > 0 && !value.match("^[0-9a-zA-Z]*$") && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.BRAZIL) {
          return new ValidationResult({
            id : 'vat',
            type : 'text',
            name : 'vat'
          }, false, 'The value for ' + lbl + ' is invalid. Only digits and alphabet combination is allowed');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

// Oject map of all the LA issuing country values and the landed country
// counterpart
var latinAmericaCntryJSON = {
  '613' : 'AR',
  '629' : 'BO',
  '631' : 'BR',
  '655' : 'CL',
  '661' : 'CO',
  '663' : 'CR',
  '681' : 'DO',
  '683' : 'EC',
  '829' : 'SV',
  '731' : 'GT',
  '735' : 'HN',
  '781' : 'MX',
  '799' : 'NI',
  '811' : 'PA',
  '813' : 'PY',
  '815' : 'PE',
  '869' : 'UY',
  '871' : 'VE',
};

function addLatinAmericaAddressHandler(cntry, addressMode, saving) {
  var latinAmericaCntryJSON2 = JSON.stringify(latinAmericaCntryJSON);
  var land1Json = JSON.parse(latinAmericaCntryJSON2);
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (!saving) {
    if (addressMode == 'newAddress') {
      /*
       * Retrieve the landed country value from the collection above
       */
      var landed = land1Json[issuingCntry];
      FilteringDropdown['val_landCntry'] = landed;
      FormManager.setValue('landCntry', landed);
      console.log("Setting address landed country to: " + landed);
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function addTaxCode1ValidatorForOtherLACntries() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var taxCd1 = FormManager.getActualValue('taxCd1');
        var lbl1 = FormManager.getLabel('LocalTax1');
        if (taxCd1 && taxCd1.length > 0 && !taxCd1.match("^[0-9a-zA-Z-]+$")) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'The value for ' + lbl1 + ' is invalid. Only digits, alphabets and dashes combination is allowed');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addSalesRepNameNoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
        var lbl1 = FormManager.getLabel('SalRepNameNo');
        if (repTeamMemberNo && repTeamMemberNo.length != 6) {
          return new ValidationResult({
            id : 'repTeamMemberNo',
            type : 'text',
            name : 'repTeamMemberNo'
          }, false, 'The value for ' + lbl1 + ' is invalid. The length should always be 6.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/**
 * Validator for tax code inputs
 */
function addTaxCodesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var currentRequestType = FormManager.getActualValue('reqType');
        if (currentRequestType != null && currentRequestType.length > 1) {
          currentRequestType = currentRequestType.substring(0, 1);
        }
        if (currentRequestType == 'C') {
          console.log(">>> EXECUTING LA addTaxCodesValidator...");
          var taxCd1 = FormManager.getActualValue('taxCd1');
          var taxCd2 = FormManager.getActualValue('taxCd2');
          var taxCd3 = FormManager.getActualValue('taxCd3');

          var lbl1 = FormManager.getLabel('LocalTax1');
          var lbl2 = FormManager.getLabel('LocalTax2');
          var lbl3 = FormManager.getLabel('LocalTax3');

          // var cannotBeEqual = taxCd1.length > 0 &&
          // taxCd1 == taxCd2;
          // if (cannotBeEqual) {
          // return new ValidationResult({
          // id : 'taxCd1',
          // type : 'text',
          // name : 'taxCd1'
          // }, false, 'The values for ' + lbl1 + '
          // and ' + lbl2 + ' cannot be
          // the same.');
          // }
          var cannotBeEqual = (taxCd1.length > 0 && taxCd1 == taxCd3);
          if (cannotBeEqual) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'The values for ' + lbl1 + ' and ' + lbl3 + ' cannot be the same.');
          }
          cannotBeEqual = (taxCd2.length > 0 && taxCd2 == taxCd3);
          if (cannotBeEqual) {
            return new ValidationResult({
              id : 'taxCd2',
              type : 'text',
              name : 'taxCd2'
            }, false, 'The values for ' + lbl2 + ' and ' + lbl3 + ' cannot be the same.');
          }

          var mustPreFill = taxCd1.length == 0 && taxCd2.length > 0;
          if (mustPreFill) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, lbl1 + ' must have a value if ' + lbl2 + ' is specified.');
          }
          mustPreFill = (taxCd1.length == 0 && taxCd3.length > 0);
          if (mustPreFill) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, lbl1 + ' must have a value if ' + lbl3 + ' is specified.');
          }
          mustPreFill = (taxCd2.length == 0 && taxCd3.length > 0);
          if (mustPreFill) {
            return new ValidationResult({
              id : 'taxCd2',
              type : 'text',
              name : 'taxCd2'
            }, false, lbl2 + ' must have a value if ' + lbl3 + ' is specified.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addBrazilAddressTypeValidator() {
  console.log("addBrazilAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.BRAZIL && FormManager.getActualValue('reqType') != 'M') {
          return new ValidationResult(null, true);
        }
        // Defect 1247806 : Changes in table value of
        // custType from "LEASE" to
        // "LEASI"
        if (FormManager.getActualValue('custType') != null && FormManager.getActualValue('custType') == 'LEASI') {
          if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 1) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult(null, false, 'Please add Installing (US) Address to this request.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/**
 * Will add vat format/pattern validator on address modal form during onLoad
 * 
 * @return {ValidationResult}
 */
function addVatValidatorInAddressModalForBrazil() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var value = FormManager.getActualValue('vat');
        var lbl = FormManager.getLabel('VAT');
        var reqType = cmr.currentRequestType;
        if (reqType != 'C') {
          if (value && value.length > 0 && !value.match("^[0-9]+$") && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.BRAZIL) {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'The value for ' + lbl + ' is invalid. Only digits are allowed.');
          }
        } else {
          var lCntry = FormManager.getActualValue('landCntry');
          if (value && value.length > 0 && !value.match("^[0-9]+$") && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.BRAZIL && lCntry == 'BR') {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'The value for ' + lbl + ' is invalid. Only digits are allowed.');
          } else if (value && value.length > 0 && !value.match("^[0-9a-zA-Z]*$") && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.BRAZIL && lCntry != '' && lCntry != 'BR') {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'The value for ' + lbl + ' is invalid. Only digits and alphabets combination is allowed.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var value = FormManager.getActualValue('vat');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (value.length > 0 && value.length != 11 && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.BRAZIL && (custSubGrp == 'PRIPE' || custSubGrp == 'IBMEM')) {
          return new ValidationResult({
            id : 'vat',
            type : 'text',
            name : 'vat'
          }, false, 'The VAT value should be of exactly 11 digits.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * @author NSE : fix to validate tax codes and vat by each address types from
 *         address grid
 * @deprecated : Will validate tax codes and vat one by one using their
 *             individual validator functions
 * @return {ValidationResult}
 */
function addTaxCodesVATFormValidatorAddrList_V2() {
  console.log("register addTaxCodesVATFormValidatorAddddrList_V2 for BR(631). . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'Tax Code 1/Tax Code 2/VAT # is required.';
                  var reqType = FormManager.getActualValue('reqType');
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], taxCd1 = '', taxCd2 = '', vat = '';
                      taxCd2 = currentAddr.taxCd2[0];
                      vat = currentAddr.vat[0];
                      if (reqType == 'C') {
                        if ((taxCd1 == '' || taxCd2 == '' || vat == '') || (taxCd1 == null || taxCd2 == null || vat == null)) {
                          errorCount++;
                          rowString += addrGridRow + " ";
                        }
                      }
                      if (reqType == 'U') {
                        if ((taxCd1 == '' || vat == '') || (taxCd1 == null || vat == null)) {
                          errorCount++;
                          rowString += addrGridRow + " ";
                        }
                      }
                    }
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records
                      // >=
                      // 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR');// validator body
}// func end

/**
 * @author NSE : Required validator for tax code one from address list grid.
 * @return {ValidationResult}
 */
function addTaxCodeOneRequiredValidatorAddrList() {
  console.log('running addTaxCodeOneRequiredValidatorAddrList . . .');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
          var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, reqType = FormManager
              .getActualValue('reqType'), genericMsg = 'Tax Code 1/State Fiscal Code is required.';
          var custTypeFlag = false;
          var _custType = FormManager.getActualValue('custType');

          if (_custType == '5COMP' || _custType == '5PRIP' || _custType == 'PRIPE' || _custType == 'IBMEM' || _custType == 'BLUEM') {
            // optional customer types
            custTypeFlag = true;
          }
          if (addressItems != null && addressItems.length != 0) {
            for ( var key in addressItems) {
              addrGridRow++;
              var currentAddr = addressItems[key], taxCd1 = '';
              taxCd1 = currentAddr.taxCd1[0];
              if ((taxCd1 == '' || taxCd1 == null) && !custTypeFlag) {
                errorCount++;
                rowString += addrGridRow + " ";
              }
            }
            var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(rowString.length - 3,
                rowString.length);
            if (Number(lastStr.trim().length) == 2) { // records
              // >=
              // 10
              beforeAnd = rowString.substring(0, rowString.length - 3);
              afterAnd = rowString.substring(rowString.length - 3, rowString.length);
            }
            if (Number(errorCount) > 0) {
              if (beforeAnd != '' && beforeAnd != null) {
                genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                return new ValidationResult(null, false, genericMsg);
              }
              genericMsg += ' Please check address list row ' + afterAnd + '.';
              return new ValidationResult(null, false, genericMsg);
            }
          }
        } else {
          console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
        }
        return new ValidationResult(null, true);
      } // validate func
    }; // return
  })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}// func end

/**
 * @author NSE : Required validator for tax code two from address list grid.
 * @return {ValidationResult}
 */
function addTaxCodeTwoRequiredValidatorAddrList() {
  console.log("running addTaxCodeTwoRequiredValidatorAddrList . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'Tax Code 2/Municipal Fiscal Code is required.';
                  var custTypeFlag = false;
                  var _custType = FormManager.getActualValue('custType');
                  if (_custType == '5COMP' || _custType == '5PRIP' || _custType == 'PRIPE' || _custType == 'IBMEM' || _custType == 'BLUEM') {
                    // optional customer types
                    custTypeFlag = true;
                  }
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], taxCd2 = '';
                      taxCd2 = currentAddr.taxCd2[0];
                      if ((taxCd2 == '' || taxCd2 == null) && reqType == 'C' && !custTypeFlag) {
                        errorCount++;
                        rowString += addrGridRow + " ";
                      }
                    }
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records
                      // >=
                      // 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}// func end

/**
 * @author NSE : Required validator for vat from address list grid.
 * @return {ValidationResult}
 */
function addVatRequiredValidatorAddrList() {
  console.log('running addVatRequiredValidatorAddrList . . .');
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'VAT #/CNPJ is required.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], vat = '';
                      vat = currentAddr.vat[0];
                      if (vat == '' || vat == null) {
                        errorCount++;
                        rowString += addrGridRow + " ";
                      }
                    }
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records
                      // >=
                      // 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}// func end

/**
 * @author DTN : Length validator for vat from address list grid.
 * @return {ValidationResult}
 */
function addVatLengthValidatorAddrList() {
  console.log('running addVatLengthValidatorAddrList . . .');
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'VAT #/CNPJ must be equal or less than 14.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], vat = '';
                      vat = currentAddr.vat[0];
                      if (vat != '' && vat != null && vat.length > VAT_LENGTH) {
                        errorCount++;
                        rowString += addrGridRow + " ";
                      }
                    }
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records
                      // >=
                      // 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}// func end

/**
 * @author NSE : #1261771 Fix to validate street address one by each address
 *         types from address grid.
 * @return {ValidationResult}
 */
function addStreetAddress1FormValidatorAddrList() {
  console.log("register addStreetAddress1FormValidatorAddrList . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'Street Address 1 is more than 30 characters.';
                  if (addressItems != null && addressItems.length != 0) {
                    var len = 30;

                    if (FormManager.getActualValue('cmrIssuingCntry') == '781') {
                      len = 40;
                    }

                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], streetAddr1 = '';
                      streetAddr1 = currentAddr.addrTxt[0];
                      if (streetAddr1 != null && streetAddr1 != '') {
                        console.log("streetAddr1 length : " + streetAddr1.length);
                        if (reqType == 'C') {
                          if (Number(streetAddr1.length) > len) {
                            errorCount++;
                            rowString += addrGridRow + " ";
                          }
                        }
                        if (reqType == 'U') {
                          var oldAddTxt = currentAddr.oldAddrText[0];
                          // if
                          // (Number(streetAddr1.length)
                          // > 30 && streetAddr1
                          // != oldAddTxt) {
                          if (Number(streetAddr1.length) > len) {
                            errorCount++;
                            rowString += addrGridRow + " ";
                          }
                        }
                      }
                    } // for
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records
                      // >=
                      // 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  onsole.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
} // func end

/**
 * @author NSE : #1261771 Fix to validate street address two by each address
 *         types from address grid.
 * @return {ValidationResult}
 */
function addStreetAddress2FormValidatorAddrList() {
  console.log("register addStreetAddress2FormValidatorAddrList . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'Street Address 2 is more than 30 characters.';
                  if (addressItems != null && addressItems.length != 0) {
                    var len = 30;

                    if (FormManager.getActualValue('cmrIssuingCntry') == '781') {
                      len = 40;
                    }

                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], streetAddr2 = '';
                      streetAddr2 = currentAddr.addrTxt2[0];
                      if (streetAddr2 != null && streetAddr2 != '') {
                        if (reqType == 'C') {
                          if (Number(streetAddr2.length) > len) {
                            errorCount++;
                            rowString += addrGridRow + " ";
                          }
                        }
                        if (reqType == 'U') {
                          var oldAddTxt2 = currentAddr.oldAddrText2[0];
                          // if
                          // (Number(streetAddr2.length)
                          // > 30 && streetAddr2
                          // != oldAddTxt2) {
                          if (Number(streetAddr2.length) > len) {
                            errorCount++;
                            rowString += addrGridRow + " ";
                          }
                        }
                      }
                    } // for
                    var beforeAnd = rowString.substring(0, rowString.length - 2);
                    var afterAnd = rowString.substring(rowString.length - 2, rowString.length);
                    var lastStr = rowString.substring(rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records
                      // >=
                      // 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR');
} // func end

function toggleAddrTypesForBR(cntry, addressMode, details) {
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
      var fRecord = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(0);
      var ttype = fRecord.addrType;
      if (typeof (ttype) == 'object') {
        ttype = ttype[0];
      }
      if (ttype == 'ZS01') {
        if (FormManager.getActualValue('reqType') == 'C') {
          if (FormManager.getActualValue('custType') && FormManager.getActualValue('custType') == 'LEASI') {
            FormManager.setValue('addrType', 'ZI01');
            cmr.showNode('radiocont_ZI01');
            cmr.hideNode('radiocont_ZS01');
          } else {
            FormManager.setValue('addrType', 'ZS01');
            cmr.showNode('radiocont_ZS01');
            cmr.hideNode('radiocont_ZI01');
          }
        } else {

        }
        cmr.hideNode('radiocont_ZD01');
        cmr.hideNode('radiocont_ZP01');
      } else if (ttype == 'ZI01') {
        FormManager.setValue('addrType', 'ZS01');
        cmr.showNode('radiocont_ZS01');
        cmr.hideNode('radiocont_ZI01');
        cmr.hideNode('radiocont_ZD01');
        cmr.hideNode('radiocont_ZP01');
      } else {

      }
    } else {
      FormManager.setValue('addrType', 'ZS01');
      cmr.showNode('radiocont_ZS01');
      cmr.hideNode('radiocont_ZI01');
      cmr.hideNode('radiocont_ZD01');
      cmr.hideNode('radiocont_ZP01');
    }
  }
  if (addressMode == 'updateAddress' || addressMode == 'copyAddress') {
    // 1164561
    if (details.ret50 != '' && details.ret50 != null) {
      FormManager.setValue('taxCd1', details.ret50);
      cmr.oldtaxcd1 = details.ret50;
    }
    // 1164558
    if (details.ret51 != '' && details.ret51 != null) {
      FormManager.setValue('taxCd2', details.ret51);
      cmr.oldtaxcd2 = details.ret51;
    }
    // 1164561
    if (details.ret52 != '' && details.ret52 != null) {
      FormManager.setValue('vat', details.ret52);
      cmr.oldvat = details.ret52;
    }
  } else if (cmr.addressMode == 'newAddress') {
    // 1164561
    FormManager.clearValue('taxCd1');
    // 1164558
    FormManager.clearValue('taxCd2');
    // 1164561
    FormManager.clearValue('vat');
  }
}
/**
 * #1278931
 */
function addLengthValidatorForCustName1() {
  console.log("register addLengthValidatorForCustName1 . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custOneVal1 = FormManager.getActualValue('mainCustNm1'), reqLengthBR = 30, lengthErrorMsg = 'Customer Name is more than 30 characters.';
        if (Number(custOneVal1.length) > reqLengthBR) {
          return new ValidationResult(FormManager.getField('mainCustNm1'), false, lengthErrorMsg);
        }
        return new ValidationResult(null, true, null);
      } // validate
    }; // return
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
} // func end

/**
 * #1278931
 */
function addLengthValidatorForCustName2() {
  console.log("register addLengthValidatorForCustName2 . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custOneVal2 = FormManager.getActualValue('mainCustNm2'), reqLengthBR = 30, lengthErrorMsg = "Customer Name Con't is more than 30 characters.";
        if (Number(custOneVal2.length) > reqLengthBR) {
          return new ValidationResult(FormManager.getField('mainCustNm2'), false, lengthErrorMsg);
        }
        return new ValidationResult(null, true, null);
      } // validate
    }; // return
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

/**
 * #1285796
 * 
 * @author NSE : Removing the validation MAX_LENGTH if role is 'Requester'
 */
function removeStAddrMaxLengthsValidator(cntry, addressMode, saving, afterValidate) {
  console.log('running removeStAddrMaxLengthsValidator for county : ' + cntry);
  if (!saving) {
    if (FormManager.GETFIELD_VALIDATIONS['addrTxt'].indexOf(Validators.MAXLENGTH) > -1) {
      FormManager.removeValidator('addrTxt', Validators.MAXLENGTH);
    }
    if (FormManager.GETFIELD_VALIDATIONS['addrTxt2'].indexOf(Validators.MAXLENGTH) > -1) {
      FormManager.removeValidator('addrTxt2', Validators.MAXLENGTH);
    }
  }
}

function addReqdFieldProcValidatorForBrazil() {
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR" && FormManager.getActualValue('reqType') == 'C') {
    var lblCollectorNameNo = FormManager.getLabel('CollectorNameNo');
    var lblSalRepNameNo = FormManager.getLabel('SalRepNameNo');
    var lblSalesBusOff = FormManager.getLabel('SalesBusOff');
    var lblSubindustry = FormManager.getLabel('Subindustry');
    var lblISIC = FormManager.getLabel('ISIC');
    var _custType = FormManager.getActualValue('custType');
    if (!(_custType == '5COMP' || _custType == '5PRIP' || _custType == 'PRIPE' || _custType == 'IBMEM')) {
      FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ lblCollectorNameNo ], 'MAIN_IBM_TAB');
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ lblSalRepNameNo ], 'MAIN_IBM_TAB');
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ lblSalesBusOff ], 'MAIN_IBM_TAB');
      FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ lblSubindustry ], 'MAIN_CUST_TAB');
      FormManager.addValidator('isicCd', Validators.REQUIRED, [ lblISIC ], 'MAIN_CUST_TAB');
    }
  }
}

function autoSetAbbrevNameForBrazil() {
  var isDisabled = dojo.byId('custSubGrp').readOnly;
  var _custType = FormManager.getActualValue('custType');

  if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
    if (_custType != 'CC3CC' && _custType != 'SOFTL' && !isDisabled) {
      var custNm1 = FormManager.getActualValue('mainCustNm1');
      if (custNm1 != '') {
        var abbreNm = custNm1.length <= 30 ? custNm1 : custNm1.substr(0, 30).trim();
        console.log(">>> AUTO SETTING ABBREV NAME TO --> " + abbreNm);
        FormManager.setValue('abbrevNm', abbreNm);
        FormManager.enable('abbrevNm');
      }
    }
  } else { // SSAMX condition
    var custNm1 = FormManager.getActualValue('mainCustNm1');
    if (custNm1 != '') {
      var abbreNm = custNm1.length <= 30 ? custNm1 : custNm1.substr(0, 30).trim();
      console.log(">>> AUTO SETTING ABBREV NAME TO --> " + abbreNm);
      FormManager.setValue('abbrevNm', abbreNm);
      FormManager.enable('abbrevNm');
    }
  }

}

function setAbbrevNameRequiredForProcessors() {
  var _custType = FormManager.getActualValue('custType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
    if (_custType == 'CC3CC') {
      if (role == 'REQUESTER') {
        FormManager.readOnly('abbrevNm');
        FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
      } else if (role == "PROCESSOR") {
        FormManager.enable('abbrevNm');
        FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
      }
    }
  }
}
/**
 * #1205934
 */
function validateAddlContactEmailField() {
  console.log('validateAddlContactEmailField : processing. . .');
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (CmrGrid.GRIDS.CONTACTINFO_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.CONTACTINFO_GRID_GRID != null) {
                  var contactStore = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.store, contactItems = contactStore._arrayOfAllItems, contGrdRow = 0, rowString = '', errorCount = 0, genericMsg = '', email1Val = FormManager
                      .getActualValue('email1');
                  if (contactItems != null && contactItems.length != 0) {
                    for ( var key in contactItems) {
                      if (FormManager.getActualValue('reqType') == 'C') {
                        genericMsg = 'Email is required and must be equal to Email1.';
                        contGrdRow++;
                        var currentContact = contactItems[key];
                        if (currentContact.contactSeqNum[0] == '001' && currentContact.contactType[0] == 'EM') {
                          if ((currentContact.contactEmail[0] != email1Val) || email1Val == '' || currentContact.contactEmail[0] == '') {
                            errorCount++;
                            rowString += contGrdRow + " ";
                          }
                        }
                      } else if (FormManager.getActualValue('reqType') == 'U' && !LAReactivate.isLAReactivateFlag()) {
                        genericMsg = 'Contact type with sequence number of 001 must be equal to Email1 if it has a value.';
                        contGrdRow++;
                        if (email1Val != '' && email1Val != null) {
                          var currentContact = contactItems[key];
                          if ((currentContact.contactType[0] != '' && currentContact.contactType[0] != null) && currentContact.contactSeqNum[0] == '001') {
                            if (currentContact.contactEmail[0] != email1Val) {
                              errorCount++;
                              rowString += contGrdRow + " ";
                            }
                          }
                        }
                      }
                    } // for
                    var beforeAnd = rowString.substring(0, rowString.length - 2);
                    var afterAnd = rowString.substring(rowString.length - 2, rowString.length);
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check contact list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check contact list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  } // if list not empty
                } else {
                  console.log("CmrGrid.GRIDS.CONTACTINFO_GRID_GRID undefined/null");
                }// grid not defined
                return new ValidationResult(null, true);
              } // validate
            };// return
          })(), 'MAIN_CONTACTINFO_TAB', 'frmCMR');
}// func end

/**
 * Contact email validation
 */
function validateAddlContactEmailFieldOtherLA() {
  console.log('validateAddlContactEmailField : processing. . .');
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (CmrGrid.GRIDS.CONTACTINFO_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.CONTACTINFO_GRID_GRID != null) {
                  var contactStore = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.store, contactItems = contactStore._arrayOfAllItems, contGrdRow = 0, rowString = '', errorCount = 0, genericMsg = '', email1Val = FormManager
                      .getActualValue('email1'), country = FormManager.getActualValue('cmrIssuingCntry');
                  if (country != SysLoc.PERU) {
                    if (contactItems != null && contactItems.length != 0) {
                      for ( var key in contactItems) {
                        if (FormManager.getActualValue('reqType') == 'C') {
                          genericMsg = 'Contact type with sequence number of 001 must be equal to Email1';
                          contGrdRow++;
                          var currentContact = contactItems[key];
                          if (currentContact.contactSeqNum[0] == '001') {
                            if ((currentContact.contactEmail[0] != email1Val) || email1Val == '' || currentContact.contactEmail[0] == '') {
                              errorCount++;
                              rowString += contGrdRow + " ";
                            }
                          }
                        } else if (FormManager.getActualValue('reqType') == 'U') {
                          genericMsg = 'Contact type with sequence number of 001 must be equal to Email1 if it has a value.';
                          contGrdRow++;
                          if (email1Val != '' && email1Val != null) {
                            var currentContact = contactItems[key];
                            if ((currentContact.contactType[0] != '' && currentContact.contactType[0] != null) && currentContact.contactSeqNum[0] == '001') {
                              if (currentContact.contactEmail[0] != email1Val) {
                                errorCount++;
                                rowString += contGrdRow + " ";
                              }
                            }
                          }
                        }
                      } // for
                      var beforeAnd = rowString.substring(0, rowString.length - 2);
                      var afterAnd = rowString.substring(rowString.length - 2, rowString.length);
                      if (Number(errorCount) > 0) {
                        if (beforeAnd != '' && beforeAnd != null) {
                          genericMsg += ' Please check contact list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                          return new ValidationResult(null, false, genericMsg);
                        }
                        genericMsg += ' Please check contact list row ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                    } // if list not empty
                  } // exclude PERU
                } else {
                  console.log("CmrGrid.GRIDS.CONTACTINFO_GRID_GRID undefined/null");
                }// grid not defined
                return new ValidationResult(null, true);
              } // validate
            };// return
          })(), 'MAIN_CONTACTINFO_TAB', 'frmCMR');
}// func end

/**
 * Validator whether DPL check has been performed. Should only be for processors
 */
/*
 * function addDPLCheckValidator() { FormManager.addFormValidator((function() {
 * return { validate : function() { var result =
 * FormManager.getActualValue('dplChkResult'); // 1. get name 1 and get old name
 * 1 var nm1 = FormManager.getActualValue("mainCustNm1"); var nm2 =
 * FormManager.getActualValue("mainCustNm2"); var oldNm1 =
 * FormManager.getActualValue("oldCustNm1"); var oldNm2 =
 * FormManager.getActualValue("oldCustNm2"); var requestType =
 * FormManager.getActualValue('reqType'); console.log("nm1 >>> " + nm1);
 * console.log("oldNm1 >>> " + oldNm1); console.log("nm2 >>> " + nm2);
 * console.log("oldNm2 >>> " + oldNm2); console.log("requestType >>> " +
 * requestType);
 * 
 * if (requestType == 'C') { if (result == '' || result.toUpperCase() == 'NOT
 * DONE') { return new ValidationResult(null, false, 'DPL Check has not been
 * performed yet.'); } else if (result == '' || result.toUpperCase() == 'ALL
 * FAILED') { return new ValidationResult(null, false, 'DPL Check has failed.
 * This record cannot be processed.'); } else { return new
 * ValidationResult(null, true); } }
 * 
 * if (requestType == 'U') { if ((result == '' || result.toUpperCase() == 'NOT
 * DONE' || result.toUpperCase() == 'NR') && (nm1 != oldNm1 || nm2 != oldNm2)) {
 * return new ValidationResult(null, false, 'DPL Check has not been performed
 * yet.'); } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
 * return new ValidationResult(null, false, 'DPL Check has failed. This record
 * cannot be processed.'); } else { return new ValidationResult(null, true); } } } };
 * })(), 'MAIN_NAME_TAB', 'frmCMR'); }
 */

function validateContactInfoGridRowCount() {
  console.log('validateContactInfoGridRowCount : processing . . .');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        if (typeof (CmrGrid.GRIDS.CONTACTINFO_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.CONTACTINFO_GRID_GRID != null) {
          if (requestType == 'C') {
            var contactCount = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.rowCount;
            if (contactCount == 0) {
              return new ValidationResult(null, false, 'Please add atleast one contact type EM.');
            } else {
              var contactStore = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.store, contactItems = contactStore._arrayOfAllItems;
              var aceCount = 0;
              for ( var key in contactItems) {
                if (contactItems[key].contactType == 'EM' && contactItems[key].contactSeqNum == '001') {
                  aceCount++;
                }
              }
              if (aceCount <= 0) {
                return new ValidationResult(null, false, 'Please add contact type EM-001.');
              }
            }
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      } // validatte
    }; // return
  })(), 'MAIN_CONTACTINFO_TAB', 'frmCMR');
}

// Rangoli's fix for defect 1468232 for mexico email1
function validateEMContact() {
  console.log('validateEMContact : processing . . .');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (custSubGrp == '5COMP' || custSubGrp == '5PRIP' || custSubGrp == 'IBMEM' || custSubGrp == 'PRIPE') {
          if (typeof (CmrGrid.GRIDS.CONTACTINFO_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.CONTACTINFO_GRID_GRID != null) {
            if (requestType == 'C') {
              var contactCount = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.rowCount;
              if (contactCount == 0) {
                return new ValidationResult(null, false, 'Please add atleast one contact type EM.');
              } else {
                var contactStore = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.store, contactItems = contactStore._arrayOfAllItems;
                var aceCount = 0;
                for ( var key in contactItems) {
                  if (contactItems[key].contactType == 'EM') {
                    aceCount++;
                  }
                }
                if (aceCount <= 0) {
                  return new ValidationResult(null, false, 'Please add atleast one contact type EM.');
                } else {
                  return new ValidationResult(null, true);
                }
              }
            }
            return new ValidationResult(null, true);
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      } // validatte
    }; // return
  })(), 'MAIN_CONTACTINFO_TAB', 'frmCMR');
}

function filterAddressesGridForBR() {
  console.log('filterAddressesGridForBR : processing from GEOHandler.executeAfterTemplateLoad. . .');
  if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
    var queryPar, currScenario = FormManager.getActualValue('custGrp'), currSub = FormManager.getActualValue('custSubGrp'), currCntry = FormManager.getActualValue('cmrIssuingCntry'), currReqId = FormManager
        .getActualValue('reqId'), addrGridFilterPathBR = cmr.CONTEXT_ROOT + '/request/address/filter.json', addrGridParamFilterBR = '';
    queryPar = {
      REQ_ID : FormManager.getActualValue('reqId')
    };
    var custTypeFrmDb = cmr.query('GET.CUSTTYPE_VALUE_FORCHECKING', queryPar), custTypeValFrmDb;
    custTypeValFrmDb = custTypeFrmDb.ret1; // if already set, do not
    // execute
    if (FormManager.getActualValue('reqType') == 'C' && custTypeValFrmDb == ''
        && (FormManager.getActualValue('findCmrResult') != 'NOT DONE' || FormManager.getActualValue('findCmrResult') != 'REJECTED')) {
      if (currScenario && currScenario != '' && currScenario != 'CROSS') {
        FormManager.setValue('saveIndAftrTempLoad', 'Y');
        if (currScenario == 'LOCAL') {
          // LOCAL CONFIG
          addrGridParamFilterBR = 'reqId=' + currReqId + '&cmrIssuingCntry=' + currCntry + '&currCustType=' + currSub;
          CmrGrid.refresh('ADDRESS_GRID', addrGridFilterPathBR, addrGridParamFilterBR);
        }
      } else {
        /*
         * FormManager.setValue('saveIndAftrTempLoad', ''); var currentPath =
         * CmrGrid.getCurrentStoreUrl('ADDRESS_GRID');
         * if(currentPath.toUpperCase() == addrGridFilterPathBRtoUpperCase()+'?' ) {
         * CmrGrid.refresh('ADDRESS_GRID'); }
         */
      }
    } else {
      if (FormManager.getActualValue('saveIndAftrTempLoad') != '') {
        FormManager.setValue('saveIndAftrTempLoad', '');
      }
    }
  }
}

function filterAddressesGridForCrossBR() {
  console.log('filterAddressesGridForCrossBR : processing from GEOHandler.executeAfterConfigs. . .');
  if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
    var queryPar, currScenario = FormManager.getActualValue('custGrp'), currCustType = FormManager.getActualValue('custType'), currCntry = FormManager.getActualValue('cmrIssuingCntry'), currReqId = FormManager
        .getActualValue('reqId'), addrGridFilterPathBR = cmr.CONTEXT_ROOT + '/request/address/filter.json', addrGridParamFilterBR = '';
    queryPar = {
      REQ_ID : FormManager.getActualValue('reqId')
    };
    var custTypeFrmDb = cmr.query('GET.CUSTTYPE_VALUE_FORCHECKING', queryPar), custTypeValFrmDb;
    custTypeValFrmDb = custTypeFrmDb.ret1; // if already set, do not
    // execute
    if (FormManager.getActualValue('reqType') == 'C' && custTypeValFrmDb == ''
        && (FormManager.getActualValue('findCmrResult') != 'NOT DONE' || FormManager.getActualValue('findCmrResult') != 'REJECTED')) {
      if (currScenario && currScenario != '' && currScenario != 'LOCAL') {
        FormManager.setValue('saveIndAftrTempLoad', 'Y');
        if (currScenario == 'CROSS') {
          // CROSS-BOARDER CONFIG
          addrGridParamFilterBR = 'reqId=' + currReqId + '&cmrIssuingCntry=' + currCntry + '&currCustType=' + currCustType;
          CmrGrid.refresh('ADDRESS_GRID', addrGridFilterPathBR, addrGridParamFilterBR);
        }
      } else {
        /*
         * FormManager.setValue('saveIndAftrTempLoad', ''); var currentPath =
         * CmrGrid.getCurrentStoreUrl('ADDRESS_GRID');
         * if(currentPath.toUpperCase() == addrGridFilterPathBRtoUpperCase()+'?' ) {
         * CmrGrid.refresh('ADDRESS_GRID'); }
         */
      }
    } else {
      if (FormManager.getActualValue('saveIndAftrTempLoad') != '') {
        FormManager.setValue('saveIndAftrTempLoad', '');
      }
    }
  }
}

function addAttachmentMandatoryValidator() {
  console.log('addAttachmentMandatoryValidator : processing from GEOHandler.registerValidator. . .');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _custType = FormManager.getActualValue('custType');
        if (_custType == '5COMP' || _custType == '5PRIP') {
          var attachmentInfoReqId = FormManager.getActualValue('reqId');
          var qParams = {
            REQ_ID : attachmentInfoReqId,
          };
          var record = cmr.query('COUNTATTACHMENTRECORDS', qParams);
          var attachmentInfoReccount = record.ret1;
          if (Number(attachmentInfoReccount) == 0) {
            return new ValidationResult(null, false, 'Since the clip level is < $5, please provide SAP screen shot.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function addVatTaxNoMandatoryValidator() {
  console.log('Inside addVatTaxNoMandatoryValidator...');
  var _custType = FormManager.getActualValue('custType');
  var _cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var lblLocalTax1 = FormManager.getLabel('LocalTax1');
  var _custGrp = FormManager.getActualValue('custGrp');
  if (_pagemodel.userRole.toUpperCase() == 'REQUESTER' && FormManager.getActualValue('reqType') == 'C' && _custGrp == "LOCAL") {
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ lblLocalTax1 ], 'MAIN_CUST_TAB');
  }
}

function setFieldRequiredSSAMXOnSecnarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var _cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (_reqType == 'C' && _custGrp != '' && (_custGrp == 'LOCAL' || _custGrp == 'CROSS') && _custSubGrp != '') {
    if (_custSubGrp != '5PRIP' && _custSubGrp != '5COMP') {
      FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');

      if (role == 'Processor' || role == 'Requester') {
        FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
        FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
        FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'SalRepNameNo' ], 'MAIN_IBM_TAB');
      }
    } else {
      FormManager.resetValidations('isicCd');
      FormManager.resetValidations('subIndustryCd');
      FormManager.resetValidations('repTeamMemberNo');
    }
    if (_cmrCntry == '781' && (_custSubGrp == '5PRIP' || _custSubGrp == '5COMP' || _custSubGrp == 'IBMEM' || _custSubGrp == 'PRIPE')) {
      FormManager.resetValidations('isicCd');
      FormManager.resetValidations('subIndustryCd');
      FormManager.resetValidations('salesBusOffCd');
      FormManager.resetValidations('collBoId');
    }
    if (role == 'PROCESSOR' && _custSubGrp == 'PRIPE') {
      FormManager.resetValidations('repTeamMemberNo');
    }

    if ((_cmrCntry == '681' || _cmrCntry == '663' || _cmrCntry == '663' || _cmrCntry == '869' || _cmrCntry == '811') && role == 'PROCESSOR' && _custSubGrp == 'PRIPE') {
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'SalRepNameNo' ], 'MAIN_IBM_TAB');
    }

  }
}

// Added by Rangoli as fix for Defect 1455791
function setCrosTypSubTypSSAMXOnSecnarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _custType = FormManager.getActualValue('custType');
  var _reqType = FormManager.getActualValue('reqType');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (_reqType == 'C' && _custGrp != '' && _custGrp == 'CROSS' && _custSubGrp != '') {
    if (_custType == 'INTER') {
      FormManager.setValue('crosTyp', '9');

      if (LAHandler.isSSAIssuingCountry(_cmrIssuingCntry)) {
        FormManager.setValue('crosSubTyp', 'UI');
      } else if (_cmrIssuingCntry == '781') {
        FormManager.setValue('crosSubTyp', 'IN');
      } else if (_cmrIssuingCntry == '631') {
        FormManager.setValue('crosSubTyp', 'CI');
      }

      console.log(">>> Process crosTyp >> " + FormManager.getActualValue('crosTyp'));
      console.log(">>> Process crosSubTyp >> " + FormManager.getActualValue('crosSubTyp'));

      if (_cmrIssuingCntry == '681' || _cmrIssuingCntry == '663') {
        FormManager.setValue('govType', '9');
        console.log(">>> Process govType >> " + FormManager.getActualValue('govType'));
      }

    } else if (_custType == 'GOVDI') {
      FormManager.setValue('crosTyp', '0');
      FormManager.setValue('crosSubTyp', 'GD');
      console.log(">>> Process crosTyp >> " + FormManager.getActualValue('crosTyp'));
      console.log(">>> Process crosSubTyp >> " + FormManager.getActualValue('crosSubTyp'));
    } else if (_custType == 'GOVIN') {
      FormManager.setValue('crosTyp', '0');
      FormManager.setValue('crosSubTyp', 'GI');
      console.log(">>> Process crosTyp >> " + FormManager.getActualValue('crosTyp'));
      console.log(">>> Process crosSubTyp >> " + FormManager.getActualValue('crosSubTyp'));
    } else if (_custType == 'INTOU') {
      FormManager.setValue('crosTyp', '9');
      FormManager.setValue('crosSubTyp', 'OU');
      console.log(">>> Process crosTyp >> " + FormManager.getActualValue('crosTyp'));
      console.log(">>> Process crosSubTyp >> " + FormManager.getActualValue('crosSubTyp'));

      if (_cmrIssuingCntry == '681' || _cmrIssuingCntry == '663') {
        FormManager.setValue('govType', '9');
        console.log(">>> Process govType >> " + FormManager.getActualValue('govType'));
      }

    } else if (_custType == 'INTUS' || _custType == 'INIBM') {
      FormManager.setValue('crosTyp', '9');
      FormManager.setValue('crosSubTyp', 'UI');
      console.log(">>> Process crosTyp >> " + FormManager.getActualValue('crosTyp'));
      console.log(">>> Process crosSubTyp >> " + FormManager.getActualValue('crosSubTyp'));

      if (_cmrIssuingCntry == '681' || _cmrIssuingCntry == '663') {
        FormManager.setValue('govType', '9');
        console.log(">>> Process govType >> " + FormManager.getActualValue('govType'));
      }
    } else {
      FormManager.setValue('crosTyp', '0');
      FormManager.setValue('crosSubTyp', 'PR');
      console.log(">>> Process crosTyp >> " + FormManager.getActualValue('crosTyp'));
      console.log(">>> Process crosSubTyp >> " + FormManager.getActualValue('crosSubTyp'));
    }
  }
}

function validateVATChile() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log("Running validateVATChile");
        var taxCd1 = FormManager.getActualValue('taxCd1');
        var lbl1 = FormManager.getLabel('LocalTax1');
        if (taxCd1 && taxCd1.length > 0 && !taxCd1.match(/^[0-9a-zA-Z]{8}-[0-9a-zA-Z]{1}$/)) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'The value for ' + lbl1 + ' is invalid. The correct Format For Chile Vat is 00000000-0');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addPostalCdValidator() {
  console.log("addPostalCdValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('custGrp') != null && FormManager.getActualValue('custGrp') == "LOCAL") {
          var postCd = FormManager.getActualValue('postCd');
          if (postCd && postCd.length > 0 && !postCd.match(/^\d{6}$/)) {
            return new ValidationResult(null, false, 'Postal Code should be 6 digits long.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateCustNameChangeForDPLCheck() {
  // CRU
  console.log("validateCustNameChangeForDPLCheck..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // If update request, if yourAction == CRU, if
        // processor
        if (typeof (_pagemodel) != 'undefined') {
          var _reqType = FormManager.getActualValue('reqType');
          var _action = FormManager.getActualValue('yourAction');
          var id = FormManager.getActualValue('reqId');
          var ret = cmr.query('GET_ADMIN_MAIN_CUST_NAMES', {
            REQ_ID : id
          });

          if (ret != null && ret.ret1 != null) {
            var _mainCustNm1 = FormManager.getActualValue('mainCustNm1');
            var _mainCustNm2 = FormManager.getActualValue('mainCustNm2');
            var _mainCustNm1Old = ret.ret1;
            var _mainCustNm2Old = ret.ret2;

            console.log("MAIN CUST NAME1 on DB >> " + _mainCustNm1Old);
            console.log("MAIN CUST NAME2 on DB >> " + _mainCustNm2Old);

            if ('U' == _reqType && (_action == 'CRU' || _action == 'VAL') && _pagemodel.userRole.toUpperCase() == 'PROCESSOR') {
              if (_mainCustNm1 != _mainCustNm1Old || _mainCustNm2 != _mainCustNm2Old) {
                return new ValidationResult(null, false, 'Customer Name or Customer Name 2 were changed. Please Save first to reflect your changes.');
              } else {
                return new ValidationResult(null, true);
              }
            } else {
              return new ValidationResult(null, true);
            }
          }
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function makeFieldManadatoryLAReactivate() {
  FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'AbbrevName' ], 'MAIN_CUST_TAB');
  var lblMrcCd = FormManager.getLabel('Market Responsibility Code (MRC)');
  if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
    FormManager.addValidator('email1', Validators.REQUIRED, [ 'Email1' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('icmsInd', Validators.REQUIRED, [ 'ICMSContribution' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
    if (_pagemodel.userRole.toUpperCase() == 'REQUESTER') {
      FormManager.resetValidations('govType');
    } else if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      FormManager.addValidator('govType', Validators.REQUIRED, [ 'GovernmentType' ], 'MAIN_CUST_TAB');
    }
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '613') {
    FormManager.addValidator('collBoId', Validators.REQUIRED, [ 'CollBranchOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '629') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '655') {
    FormManager.addValidator('busnType', Validators.REQUIRED, [ 'BusinessType' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '661') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '663') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '681') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '683') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collBoId', Validators.REQUIRED, [ 'CollBranchOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '829') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '731') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '735') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '781') {
    if (_pagemodel.userRole.toUpperCase() == 'REQUESTER') {
      FormManager.setValue('collBoId', '103');
      FormManager.readOnly('collBoId');
      FormManager.setValue('repTeamMemberNo', '111111');
      FormManager.readOnly('repTeamMemberNo');
    } else if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      FormManager.addValidator('collBoId', Validators.REQUIRED, [ 'CollBranchOff' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
    }
    FormManager.addValidator('addrTxt', Validators.REQUIRED, [ 'StreetAddress1' ], '');
    FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'StateProv' ], '');
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'PostalCode' ], '');
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'LocalTax1' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'SalRepNameNo' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '799') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '811') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '813') {
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '815') {
    FormManager.addValidator('email1', Validators.REQUIRED, [ 'Email1' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '869') {
    FormManager.addValidator('collBoId', Validators.REQUIRED, [ 'CollBranchOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '871') {
    FormManager.addValidator('mrktChannelInd', Validators.REQUIRED, [ 'MrktChannelInd' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collBoId', Validators.REQUIRED, [ 'CollBranchOff' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectorNameNo', Validators.REQUIRED, [ 'CollectorNameNo' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SalesBusOff' ], 'MAIN_IBM_TAB');
  }
}

function validateAddlContactEmailFieldForReactivate() {
  console.log('validateAddlContactEmailFieldForReactivate : processing. . .');
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (CmrGrid.GRIDS.CONTACTINFO_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.CONTACTINFO_GRID_GRID != null && LAReactivate.isLAReactivateFlag()) {
                  var contactStore = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID.store, contactItems = contactStore._arrayOfAllItems, contGrdRow = 0, rowString = '', errorCount = 0, genericMsg = '', email1Val = FormManager
                      .getActualValue('email1');
                  if (contactItems != null && contactItems.length != 0) {
                    for ( var key in contactItems) {
                      genericMsg = 'Contact type with sequence number of 001 must be equal to Email1 if it has a value.';
                      contGrdRow++;
                      var currentContact = contactItems[key];
                      if (currentContact.contactSeqNum[0] == '001' && currentContact.contactType[0] == 'LE') {
                        if ((currentContact.contactEmail[0] != email1Val) || email1Val == '' || currentContact.contactEmail[0] == '') {
                          errorCount++;
                          rowString += contGrdRow + " ";
                        }
                      }

                    } // for
                    var beforeAnd = rowString.substring(0, rowString.length - 2);
                    var afterAnd = rowString.substring(rowString.length - 2, rowString.length);
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check contact list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check contact list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  } // if list not empty
                } else {
                  console.log("CmrGrid.GRIDS.CONTACTINFO_GRID_GRID undefined/null");
                }// grid not defined
                return new ValidationResult(null, true);
              }
            };
          })(), 'MAIN_CONTACTINFO_TAB', 'frmCMR');
}

function addDPLCheckValidatorLAReactivate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('dplChkResult');
        if (LAReactivate.isLAReactivateFlag()) {
          // var result =
          // FormManager.getActualValue('dplChkResult');
          console.log('>>> RUNNING LA addDPLCheckValidator');
          if (result == '' || result.toUpperCase() == 'NR') {
            return new ValidationResult(null, false, 'DPL Check has not been performed yet.');
          } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
            return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          if (result == '' || result.toUpperCase() == 'ALL FAILED') {
            return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function disableFieldsForBrazil() {
  console.log("disableFieldsForBrazil...");
  FormManager.readOnly('addrTxt2');
}

function canRemoveAddress(value, rowIndex, grid) {
  console.log("Remove address button..");
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    return false;
  } else {
    // var addrType = rowData.addrType;
    // if (addrType == 'ZS01') {
    // return false;
    // }
    return true;
  }
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

/* 1640462 - MRC should be optional for requester for SSA&MX */
function setMrcCdRequiredForProcessors() {
  var _country = FormManager.getActualValue('cmrIssuingCntry');
  console.log('****Executiing setMrcCdRequiredForProcessors for country ' + _country);
  if (_country != '631') {
    if (typeof (_pagemodel) != 'undefined') {
      if (_pagemodel.userRole == 'Processor') {
        var lblMrcCd = FormManager.getLabel('MrcCd');
        FormManager.addValidator('mrcCd', Validators.REQUIRED, [ lblMrcCd ], 'MAIN_IBM_TAB');
      } else {
        FormManager.resetValidations('mrcCd');
      }
    }
  }
}

// MK: Defect 1736631: BR: MRC cannot be mandatory for requesters in Update
function setMrcCdOptionalForRequester() {
  var _country = FormManager.getActualValue('cmrIssuingCntry');
  var requestType = FormManager.getActualValue('reqType');
  console.log('****Executiing setMrcCdOptionalForRequester for country ' + _country);
  if (_country == '631' && requestType == 'U') {
    if (typeof (_pagemodel) != 'undefined') {
      if (_pagemodel.userRole.toUpperCase() == 'REQUESTER') {
        FormManager.resetValidations('mrcCd');
      }
    }
  }
}

function setLocationNumberForBR(cntry, addressMode, saving, finalSave, force) {
  var issuecountry = FormManager.getActualValue('cmrIssuingCntry');
  var stateProv = FormManager.getActualValue('stateProv');
  if (issuecountry == '631' && stateProv == 'RS') {
    FormManager.setValue('locationNumber', '91000');
  }
}

function setLocNoLockedForRequesterBR() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('locationNumber');
  } else if (role == 'PROCESSOR') {
    FormManager.enable('locationNumber');
  }
}

/* Register LA Validators */
dojo.addOnLoad(function() {
  GEOHandler.LA = [ SysLoc.ARGENTINA, SysLoc.BOLIVIA, SysLoc.BRAZIL, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA, SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR, SysLoc.GUATEMALA, SysLoc.HONDURAS,
      SysLoc.MEXICO, SysLoc.NICARAGUA, SysLoc.PANAMA, SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY, SysLoc.VENEZUELA ];
  console.log(GEOHandler.LA);
  console.log('adding LA scripts...');
  // GEOHandler.registerValidator(addTaxInfoValidator, [ SysLoc.ARGENTINA ],
  // null, false, false);
  GEOHandler.registerValidator(addTaxInfoValidator, [ SysLoc.ARGENTINA /* 613 */], GEOHandler.ROLE_PROCESSOR, false, false);
  // Story 1202261 : Address Type validation for Brazil
  GEOHandler.registerValidator(addBrazilAddressTypeValidator, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addDPLCheckValidatorLAReactivate, GEOHandler.LA, GEOHandler.ROLE_PROCESSOR, false, false);

  GEOHandler.addAddrFunction(addLatinAmericaAddressHandler, GEOHandler.LA);
  GEOHandler.registerValidator(addTaxCode1ValidatorForOtherLACntries, SSAMX_COUNTRIES, null, false, false);
  // GEOHandler.registerValidator(addTaxCodesValidator, GEOHandler.LA);
  // GEOHandler.registerValidator(addTaxCodesValidator, [ SysLoc.BRAZIL ],
  // null,
  // false, false);
  GEOHandler.registerValidator(addSalesRepNameNoValidator, GEOHandler.LA);
  // /-- addressModal
  GEOHandler.registerValidator(addTaxCode1ValidatorInAddressModalForBrazil, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addTaxCode2ValidatorInAddressModalForBrazil, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addVatValidatorInAddressModalForBrazil, [ SysLoc.BRAZIL ], null, true);
  // addressModal --/
  // /-- addressGrid
  GEOHandler.registerValidator(addTaxCodeOneRequiredValidatorAddrList, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addTaxCodeTwoRequiredValidatorAddrList, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addVatRequiredValidatorAddrList, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addVatLengthValidatorAddrList, [ SysLoc.BRAZIL ], null, true);
  GEOHandler.registerValidator(addStreetAddress1FormValidatorAddrList, GEOHandler.LA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addStreetAddress2FormValidatorAddrList, GEOHandler.LA, GEOHandler.ROLE_PROCESSOR, true);
  // addressGrid --/
  GEOHandler.addAfterConfig(afterConfigForLA, GEOHandler.LA);
  GEOHandler.addAfterConfig(setMrcCdRequiredForProcessors, SSAMX_COUNTRIES);
  GEOHandler.registerValidator(addVatValidatorForOtherLACountries, [ '629', '655', '661', '663', '681', '683', '731', '735', '781', '799', '811', '813', '815', '829', '869', '871' ], null, false,
      false);
  GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForBR, [ SysLoc.BRAZIL ]);
  GEOHandler.registerValidator(addLengthValidatorForCustName1, GEOHandler.LA, null, true);
  GEOHandler.registerValidator(addLengthValidatorForCustName2, GEOHandler.LA, null, true);
  GEOHandler.checkRoleBeforeAddAddrFunction(removeStAddrMaxLengthsValidator, [ SysLoc.BRAZIL ], null, GEOHandler.ROLE_REQUESTER);
  // Customer tab
  // Subindustry
  GEOHandler.registerValidator(addSubindustryValidator, GEOHandler.LA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterTemplateLoad(addReqdFieldProcValidatorForBrazil, [ SysLoc.BRAZIL ]);
  GEOHandler.registerValidator(validateAddlContactEmailField, [ SysLoc.BRAZIL, SysLoc.PERU ], null, true);
  /*
   * GEOHandler.registerValidator(validateAddlContactEmailFieldOtherLA,
   * SSAMX_COUNTRIES, null, true);
   */
  GEOHandler.registerValidator(validateContactInfoGridRowCount, [ SysLoc.BRAZIL ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(validateEMContact, [ SysLoc.MEXICO ], null, true);
  GEOHandler.addAfterTemplateLoad(filterAddressesGridForBR, [ SysLoc.BRAZIL ]);
  GEOHandler.registerValidator(addAttachmentMandatoryValidator, [ SysLoc.BRAZIL, SysLoc.MEXICO ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.addAfterTemplateLoad(addVatTaxNoMandatoryValidator, SSAMX_COUNTRIES);
  GEOHandler.addAfterTemplateLoad(setFieldRequiredSSAMXOnSecnarios, SSAMX_COUNTRIES);
  // GEOHandler.addAfterTemplateLoad(setCrosTypSubTypSSAMXOnSecnarios,
  // SSAMX_COUNTRIES);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.LA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(validateVATChile, [ SysLoc.CHILE ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addPostalCdValidator, [ SysLoc.ECUADOR ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(validateCustNameChangeForDPLCheck, GEOHandler.LA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(validateAddlContactEmailFieldForReactivate, [ SysLoc.BRAZIL ], GEOHandler.ROLE_PROCESSOR, true);

  // GEOHandler.addAfterConfig(disableFieldsForBrazil, [ SysLoc.BRAZIL ]);
  // 
  /* 1640462 - MRC should be optional for requester for SSA&MX */
  GEOHandler.addAfterTemplateLoad(setMrcCdRequiredForProcessors, SSAMX_COUNTRIES);
  GEOHandler.addAfterConfig(setMrcCdOptionalForRequester, [ SysLoc.BRAZIL ]);
  GEOHandler.addAfterTemplateLoad(setMrcCdOptionalForRequester, [ SysLoc.BRAZIL ]);
  GEOHandler.addAddrFunction(setLocationNumberForBR, GEOHandler.LA);
  GEOHandler.addAfterConfig(setLocNoLockedForRequesterBR, [ SysLoc.BRAZIL ]);

});
