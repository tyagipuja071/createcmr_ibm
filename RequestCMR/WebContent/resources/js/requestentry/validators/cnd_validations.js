/**
 * 
 */
/* Register CND Javascript */
var _vatExemptHandler = null;
var _membLvlBPRelTypeHandler=null;
var _taxExemptHandler = null;
var _isuHandler = null;

function afterConfigForCND() {
  
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      console.log(">>> RUNNING!!!!");
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process vatExempt remove * >> ");
        FormManager.resetValidations('vat');
        FormManager.resetValidations('taxCd1');
      } else {
        console.log(">>> Process vatExempt add * >> ");
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
        FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'LocalTax1' ], 'MAIN_CUST_TAB');
      }
    });
  }
  
  window.setTimeout(setDefaultFieldValuesForCND, 6000);
  
  if (_membLvlBPRelTypeHandler == null) {
    window.setTimeout(resetMembLvlBpRelType, 1500);
    _membLvlBPRelTypeHandler = dojo.connect(FormManager.getField('ppsceid'), 'onChange', function(value) {
      resetMembLvlBpRelType();
    });
  }
  
  if (_isuHandler == null && FormManager.getField('isuCd')) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues();
    });
  }
  // CREATCMR-788
  addressQuotationValidatorCND();
}

function modeOfPaymentAndOrderBlockCdHandling() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    FormManager.addValidator('modeOfPayment', Validators.REQUIRED, [ 'Credit Code' ], 'MAIN_CUST_TAB');
    // FormManager.addValidator('ordBlk', Validators.REQUIRED, [ 'Order Block
    // Code' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.resetValidations('modeOfPayment');
    FormManager.setValue('modeOfPayment', 'A001');
    FormManager.resetValidations('ordBlk');
    FormManager.readOnly('modeOfPayment');
    FormManager.readOnly('ordBlk');
  }
}

function setClientTierValues() {
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
    FormManager.enable('clientTier');
  }
}

function autoSetTax(){
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (dijit.byId('vatExempt').get('checked')) {
    FormManager.resetValidations('vat');
    FormManager.resetValidations('taxCd1');
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'LocalTax1' ], 'MAIN_CUST_TAB');
  }
}

function setDefaultFieldValuesForCND(){
  if(FormManager.getActualValue('reqType') == 'C'){
    if (!PageManager.isReadOnly()) {
      FormManager.enable('clientTier');
    }
    if (FormManager.getActualValue('isuCd') == '5K') {
      setClientTierValues();
      return;
    }
    if (typeof (_pagemodel) != 'undefined'){
      if((_pagemodel['custClass'] == null) || (_pagemodel['custClass'] == '')) {
         FormManager.setValue('custClass', '11');
      }
      if((_pagemodel['isuCd'] == null) || (_pagemodel['isuCd'] == '')) {
        FormManager.setValue('isuCd', '34');
      }
      if((_pagemodel['clientTier'] == null) || (_pagemodel['clientTier'] == '')) {
        FormManager.setValue('clientTier', 'V');
      }
      if((_pagemodel['privIndc'] == null) || (_pagemodel['privIndc'] == '')) {
        FormManager.setValue('privIndc', 'W');
      }
      }
  }  
}

function addCNDLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function setPrivacyIndcReqdForProc(){
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR" && FormManager.getActualValue('reqType') == 'C'){
  FormManager.addValidator('privIndc', Validators.REQUIRED, [ 'PrivacyIndc' ], 'MAIN_CUST_TAB');
  }
}

function resetMembLvlBpRelType(){
  if(FormManager.getActualValue('reqType') == 'U'){
    if(FormManager.getActualValue('ppsceid')!=null && FormManager.getActualValue('ppsceid')!=''){
      FormManager.resetValidations('memLvl');  
      FormManager.resetValidations('bpRelType'); 
    }
  }
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

        if (reqType == 'C' && (clientTier == "4" ||clientTier == "6"|| clientTier == "A" || clientTier == "B" || clientTier == "M"|| clientTier == "V" || clientTier == "Z" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C"  || clientTier == "0")) {
           return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid value from list.');
          } else if(reqType == 'U'&& oldCtc != null && oldCtc != clientTier && (clientTier == "4" ||clientTier == "6" || clientTier == "A" || clientTier == "B" ||clientTier == "M"|| clientTier == "V" || clientTier == "Z" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C" || clientTier == "0"))         
           {
            return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
           } else {
             return new ValidationResult(null, true);
            }
          }
        }
    })(), 'MAIN_IBM_TAB', 'frmCMR');
 }

//  Defect 1370022: By Mukesh
function canRemoveAddress(value, rowIndex, grid) {
  console.log("Remove address button..");
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return ''; 
  }
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;
  
  var reqType = FormManager.getActualValue('reqType');
     if(reqType == 'U'){
        if(importInd == 'Y'){
         return false;
        }else{
         return true;
        }
    } else{
      return true;
    }   
}
function canUpdateAddress(value, rowIndex, grid) {
  return true;
}
function canCopyAddress(value, rowIndex, grid) {
  return true;
}
function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return ''; 
  }
  rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;
  var reqType = FormManager.getActualValue('reqType');
     if(reqType == 'U'){
        if(importInd == 'Y'){
         return false;
        }else{
         return true;
        }
    } else{
      return true;
    }   
}   
function addressQuotationValidatorCND() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name 1' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name 2' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Customer Name 3' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Customer Name 4' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Department' ]);

  FormManager.addValidator('floor', Validators.NO_QUOTATION, [ 'Floor' ]);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'Building' ]);
  FormManager.addValidator('office', Validators.NO_QUOTATION, [ 'Office' ]);
  FormManager.addValidator('city2', Validators.NO_QUOTATION, [ 'District' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);

  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PostBox' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
  FormManager.addValidator('transportZone', Validators.NO_QUOTATION, [ 'Transport Zone' ]);

}

dojo.addOnLoad(function() {
  GEOHandler.CND = [ SysLoc.BAHAMAS, SysLoc.BARBADOS, SysLoc.BERMUDA, SysLoc.CAYMAN_ISLANDS, SysLoc.GUYANA , SysLoc.JAMAICA, SysLoc.SAINT_LUCIA, SysLoc.NETH_ANTILLES, SysLoc.SURINAME, SysLoc.TRINIDAD_TOBAGO ];
  console.log('adding CND validators...');
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.CND);
  GEOHandler.addAfterConfig(afterConfigForCND, GEOHandler.CND);
  GEOHandler.addAfterConfig(autoSetTax, SysLoc.BAHAMAS);
  //DENNIS: COMMENTED BECAUSE THIS IS IN DUPLICATE OF THE VALIDATOR REGISTERED ON WW
//  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.CND, GEOHandler.ROLE_PROCESSOR, false, false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.CND);
  GEOHandler.addAddrFunction(addCNDLandedCountryHandler, GEOHandler.CND);
//  GEOHandler.addAfterConfig(setDefaultFieldValuesForCND, GEOHandler.CND);
  GEOHandler.addAfterConfig(setPrivacyIndcReqdForProc, GEOHandler.CND);
  GEOHandler.addAfterConfig(resetMembLvlBpRelType, GEOHandler.CND);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.CND );
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.CND );
  GEOHandler.registerValidator(addCtcObsoleteValidator,GEOHandler.CND , null, true);
  GEOHandler.addAfterConfig(modeOfPaymentAndOrderBlockCdHandling, GEOHandler.CND);
});
