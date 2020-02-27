/**
 * 
 */
/* Register CND Javascript */
var _vatExemptHandler = null;
var _membLvlBPRelTypeHandler=null;
var _taxExemptHandler = null;

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
});