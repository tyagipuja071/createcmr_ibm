/*
 * File: taxinfo.js
 * Description: 
 * Contains the functions related to taxinfo modal functions.
 */

/**
 * Formats the  Tax Type
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function taxCodeFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var geoTaxInfoId=rowData.geoTaxInfoId;
  return '<a href="javascript: openTaxInfoDetails(\'' + reqId + '\', \'' + geoTaxInfoId +'\')">' + value + '</a>';
}

/**
 * Formatter for taxinfo List Actions column
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function taxinfoFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var taxCd = rowData.taxCd;
  var geoTaxInfoId=rowData.geoTaxInfoId;
  var actions = '';  
  actions += '<input type="button" value="Copy" class="cmr-grid-btn" onclick="doCopyTaxInfo(\'' + reqId + '\',\'' + geoTaxInfoId + '\')">';
  actions += '<input type="button" value="Update" class="cmr-grid-btn" onclick="doUpdateTaxInfo(\'' + reqId + '\',\'' + geoTaxInfoId + '\')">';
  actions += '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveTaxInfo(\'' + reqId + '\',\'' + geoTaxInfoId + '\')">';
  return actions;
  
}

/**
 * Opens the Tax Info Details screen (view only)
 * 
 * @param TaxCd
 */
function openTaxInfoDetails(reqId, geoTaxInfoId) {
  var qParams = {
      REQ_ID : reqId,
      GEO_TAX_INFO_ID : geoTaxInfoId,
  };
  var result = cmr.query('TAXINFODETAIL', qParams);
  cmr.taxinfodetails = result;
  cmr.showModal('TaxInfoDetailsModal');
}

/**
 * When the TaxInfo Details (view only) modal is shown, connect to the DB and
 * retrieve the values
 */
function TaxInfoDetailsModal_onLoad() {
  var details = cmr.taxinfodetails;
  dojo.query('#TaxInfoDetailsModal #taxCd_view')[0].innerHTML = details.ret4;
  if (FormManager.getActualValue('cmrIssuingCntry') == '613') {
  dojo.query('#TaxInfoDetailsModal #taxNum_view')[0].innerHTML = details.ret3;
  }
  dojo.query('#TaxInfoDetailsModal #taxSeparationIndc_view')[0].innerHTML = details.ret5;
  dojo.query('#TaxInfoDetailsModal #contractPrintIndc_view')[0].innerHTML = details.ret7;
  dojo.query('#TaxInfoDetailsModal #billingPrintIndc_view')[0].innerHTML = details.ret6;
  dojo.query('#TaxInfoDetailsModal #cntryUse_view')[0].innerHTML = details.ret8;
  FormManager.setValue('view_reqId', details.ret1);
  FormManager.setValue('view_geoTaxInfoId', details.ret2);
}

function TaxInfoDetailModal_onClose() {
}

/**
 * Executed when Update is clicked from Tax Info Details modal
 */
function doUpdateTaxInfoFromDetails() {
  cmr.hideModal('TaxInfoDetailsModal');
  var req_id = FormManager.getActualValue('view_reqId');
  var geoTaxInfo_id = FormManager.getActualValue('view_geoTaxInfoId');
  doUpdateTaxInfo(req_id, geoTaxInfo_id);
}

/**
 * Called for cancel add/edit tax info
 */
function cancelTaxInfoModal() {
  if (cmr.taxinfoMode == 'updateTaxInfo') {
    cmr.showConfirm('actualCancelTaxInfoModal()', 'Exit without saving this Tax Info row?');
  } else {
    cmr.showConfirm('actualCancelTaxInfoModal()', 'Exit without saving this new Tax Info row?');
  }
}
/**
 * Called for cancel add/edit  taxinfo
 */
function actualCancelTaxInfoModal() {
  cmr.hideModal('addEditTaxInfoModal');
  MessageMgr.clearMessages(true);
}

/**
 * The 'save' function for tax info records
 */
function doAddToTaxInfoList() {
  cmr.currentModalId = 'addEditTaxInfoModal';
  cmr.taxinfoReqId = FormManager.getActualValue('reqId');
  cmr.geoTaxInfoId=FormManager.getActualValue('geoTaxInfoId');
  cmr.taxCd=FormManager.getActualValue('taxCd');
  var qParams;
  var dummygeoTaxInfoId = 0;
  if (FormManager.validate('frmCMR_taxInfoModal', true)) {
    if (cmr.taxinfoMode == 'updateTaxInfo') {
      qParams = {
        REQ_ID : cmr.taxinfoReqId,
        GEO_TAX_INFO_ID : cmr.geoTaxInfoId,
      };
    } else {
      qParams = {
        REQ_ID : cmr.taxinfoReqId,
        GEO_TAX_INFO_ID : dummygeoTaxInfoId,
      };
    }
    qParams1 = {
        REQ_ID : cmr.taxinfoReqId,
        TAX_CD : cmr.taxCd,
      };
  var showTaxInfoExceedError= false;
  var showDuplicateTaxCodeError=false;
    var result = cmr.query('GETTAXINFORECORDS', qParams);
    var result1 = cmr.query('GETRECORDSFORTAXCODE', qParams1);
    var taxinfocount = result.ret1;
    var recordcount=result1.ret1;
    if (cmr.taxinfoMode != 'updateTaxInfo'){
    showDuplicateTaxCodeError= Number(recordcount) >0;
    }
    showTaxInfoExceedError = Number(taxinfocount) >= 5 ;
    if (showTaxInfoExceedError) {
        cmr.showAlert('This request already has 5 Tax Info records , Max 5 entries are allowed. You will have to remove one before completing the request processing.');
        cmr.hideModal('addEditTaxInfoModal');
        return;
    } else if(showDuplicateTaxCodeError){ 
      cmr.showAlert('This request already has an existing record with Tax Type='+cmr.taxCd+'. New Record cannot be inserted with duplicate Tax Type.');
      cmr.hideModal('addEditTaxInfoModal');
      return;  
      }else {
        actualAddToTaxInfoList();
        return;
      }
    }
}

/**
 * Does the actual adding to the list
 */
function actualAddToTaxInfoList() {
  cmr.taxinfoReqId = FormManager.getActualValue('reqId');
  cmr.currentModalId = 'addEditTaxInfoModal';
  cmr.taxCd=FormManager.getActualValue('taxCd');
  cmr.taxSeparationIndc=FormManager.getActualValue('taxSeparationIndc');
  cmr.contractPrintIndc=FormManager.getActualValue('contractPrintIndc');
  cmr.billingPrintIndc=FormManager.getActualValue('billingPrintIndc');
  cmr.cntryUse=FormManager.getActualValue('cntryUse');
  if(cmr.taxCd=="" && cmr.taxSeparationIndc=="" && cmr.contractPrintIndc=="" && cmr.billingPrintIndc==""  && cmr.cntryUse=="" )
    {
    cmr.showAlert("All the Tax fields are empty, so cannot insert/update the record");
    cmr.hideModal('addEditTaxInfoModal');
    return;
    }else{
  if (cmr.taxinfoMode == 'newTaxInfo' || cmr.taxinfoMode == 'copyTaxInfo') {
    FormManager.doHiddenAction('frmCMR_taxInfoModal', 'ADD_TAXINFO', cmr.CONTEXT_ROOT + '/request/taxinfo/process.json?reqId=' + cmr.taxinfoReqId, true, refreshTaxInfoAfterResult, true);
  } else if (cmr.taxinfoMode == 'updateTaxInfo') {
    FormManager.doHiddenAction('frmCMR_taxInfoModal', 'UPDATE_TAXINFO', cmr.CONTEXT_ROOT + '/request/taxinfo/process.json?reqId=' + cmr.taxinfoReqId, true, refreshTaxInfoAfterResult, true);
  }
    }
}

/**
 * Refreshes the TaxInfo grid after saving
 * 
 * @param result
 */
function refreshTaxInfoAfterResult(result) {
  if (result.success) {
    if (cmr.taxinfoMode == 'newTaxInfo' || cmr.taxinfoMode == 'copyTaxInfo') {
      FormManager.setValue('showTaxInfo', 'Y');
      if (cmr.currentRequestType) {
        FormManager.setValue('reqType', cmr.currentRequestType);
      }
      cmr.noCreatePop = 'N'; 
      cmr.hideModal('addEditTaxInfoModal');
    } else {
      if (cmr.taxinfoMode == 'updateTaxInfo') {
          FormManager.setValue('showTaxInfo', 'Y');
        }
      if (cmr.taxinfoMode != 'removeTaxInfo') {
        cmr.hideModal('addEditTaxInfoModal');
      }
    }
      CmrGrid.refresh('TAXINFO_GRID');
    
    }else {
    dojo.query('#dialog_addEditTaxInfoModal div.dijitDialogPaneContent')[0].scrollTo(0, 0);
  }
}
 

/**
 * Adds a new Tax Info to the list
 */
function doAddTaxInfo() {
  cmr.taxinfoMode = 'newTaxInfo';
  cmr.taxinfoReqId = FormManager.getActualValue('reqId');
  var qParams;
  var taxinfocount=0;
      qParams = {
        REQ_ID : cmr.taxinfoReqId,
      };
    var result = cmr.query('COUNTTAXINFORECORDS', qParams);
    taxinfocount = result.ret1;
  if (Number(taxinfocount) >=5) {
    cmr.showAlert('This request already has 5 Tax Info records , Max 5 entries are allowed. You will have to remove one before completing the request processing.');
    return;
    }else {
      cmr.showModal('addEditTaxInfoModal');   
    }
}
cmr.noCreatePop = 'N';
/**
 * Onload function for TaxInfo Add/Update screen Connects to the DB and
 * retrieves the current values
 */
function addEditTaxInfoModal_onLoad() {
    if (cmr.taxinfoMode == 'newTaxInfo' || cmr.taxinfoMode == 'copyTaxInfo') {
    cmr.currentRequestType = FormManager.getActualValue('reqType');
    if (cmr.currentRequestType != null && cmr.currentRequestType.length > 1) {
      cmr.currentRequestType = cmr.currentRequestType.substring(0, 1);
    }
    cmr.noCreatePop = 'Y';
    if (cmr.currentRequestType != 'C') {
      FormManager.setValue('reqType', 'C');
    }
    FormManager.enable('taxCd');
  }
  
  if (cmr.taxinfoMode == 'newTaxInfo') {
    FormManager.clearValue('taxCd');
    FormManager.clearValue('taxNum');
    FormManager.clearValue('taxSeparationIndc');
    FormManager.clearValue('billingPrintIndc');
    FormManager.clearValue('contractPrintIndc');
    FormManager.clearValue('cntryUse');
    cmr.setModalTitle('addEditTaxInfoModal', 'Add an Tax Info');
    dojo.byId('taxInfoBtn').value = 'Save Tax Info';
  } else if (cmr.taxinfoMode == 'copyTaxInfo' || cmr.taxinfoMode == 'updateTaxInfo' || cmr.taxinfoMode == 'removeTaxInfo') {
    var details = cmr.taxinfodetails;
    FormManager.setValue('taxCd', details.ret4);
    FormManager.setValue('taxNum', details.ret3);
    FormManager.setValue('taxSeparationIndc', details.ret5);
    FormManager.setValue('billingPrintIndc', details.ret6);
    FormManager.setValue('contractPrintIndc', details.ret7);
    FormManager.setValue('cntryUse', details.ret8);
    FormManager.setValue('geoTaxInfoId',details.ret2);
    if (cmr.taxinfoMode == 'copyTaxInfo') {
      cmr.setModalTitle('addEditTaxInfoModal', 'Copy an Tax Info');
      FormManager.setValue(parseInt("0"));
    } else {
      cmr.setModalTitle('addEditTaxInfoModal', 'Update an Tax Info');
      FormManager.readOnly('taxCd');
    }
    dojo.byId('taxInfoBtn').value = 'Save Tax Info';

  }
}

/**
 * Executed when the add/edit tax info modal is closed
 */
function addEditTaxInfoModal_onClose() {
  if (cmr.currentRequestType) {
    FormManager.setValue('reqType', cmr.currentRequestType);
  }
  cmr.noCreatePop = 'N';
}

/**
 * Opens the Tax Info Details screen
 * 
 * @param reqId, geoTaxInfoId
 */
function doCopyTaxInfo(reqId,geoTaxInfoId) {
  var qParams = {
    REQ_ID : reqId,
    GEO_TAX_INFO_ID : geoTaxInfoId,
  };
  var result = cmr.query('TAXINFODETAIL', qParams);
  cmr.taxinfodetails = result;
  cmr.taxinfoMode = 'copyTaxInfo';
  cmr.showModal('addEditTaxInfoModal');
}

/**
 * Opens the Tax Info Details screen for update
 * 
 * @param TaxCd
 */
function doUpdateTaxInfo(reqId, geoTaxInfoId) {
  var qParams = {
    REQ_ID : reqId,
    GEO_TAX_INFO_ID : geoTaxInfoId,
  };
  var result = cmr.query('TAXINFODETAIL', qParams);
  cmr.taxinfodetails = result;
  cmr.taxinfoMode = 'updateTaxInfo';
  cmr.showModal('addEditTaxInfoModal');
}

/**
 * Remove one Tax Info from the list via the remove button
 */
function doRemoveTaxInfo(reqId,geoTaxInfoId) {
  cmr.removeDetails = {
    remReqId : reqId,
    remGeoTaxInfoId : geoTaxInfoId
  
  };
  cmr.showConfirm('actualRemoveTaxInfo()', 'Remove the selected row from Tax Info?');

}

/**
 * Opens the Tax Info Details screen
 * 
 * @param TaxCd
 */
function actualRemoveTaxInfo() {
  cmr.taxinfoMode = 'removeTaxInfo';
  FormManager.setValue('reqId', cmr.removeDetails.remReqId);
  FormManager.setValue('geoTaxInfoId', cmr.removeDetails.remGeoTaxInfoId);
  cmr.taxinfoReqId = FormManager.getActualValue('reqId');
  cmr.taxinfoGeoTAxInfoId=FormManager.getActualValue('geoTaxInfoId');
  FormManager.doHiddenAction('frmCMR_taxInfoModal', 'REMOVE_TAXINFO', cmr.CONTEXT_ROOT + '/request/taxinfo/process.json?reqId=' + cmr.taxinfoReqId, true, refreshTaxInfoAfterResult, false);
}

