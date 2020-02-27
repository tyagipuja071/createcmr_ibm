/*
 * Javascript for user preferences pages
 */

/**
 * Adds a delegate to the list
 */
function doAddDelegate() {
  if (FormManager.validate('frmCMRDel')) {
    cmr.showConfirm('actualAddDelegate()', 'Add <strong>' + FormManager.getActualValue('delegateNm') + '</strong> to the Delegate List?');
  }
}

/**
 * Called after the confirm
 */
function actualAddDelegate() {
  cmr.addaction = true;
  FormManager.doHiddenAction('frmCMRDel', 'ADD_DELEGATE', cmr.CONTEXT_ROOT + '/preferences/delegate.json', false, refreshAfterResult);
}

/**
 * 
 * @param result
 */
function refreshAfterResult(result) {
  if (result.success) {
    if (cmr.addaction && !_profilecomplete) {
      window.setTimeout('completeProfile()', 1500);
    }
    CmrGrid.refresh('DELEGATE_GRID');
  }
}

function completeProfile() {
  FormManager.save('frmCMR', 'Completing your profile, please wait.');
  // window.location = cmr.CONTEXT_ROOT + '/preferences?infoMessage=Your profile
  // is complete.';
}

/**
 * Removes the delegate with the given ID and name
 * 
 * @param id
 * @param name
 */
function doRemoveDelegate(id, name) {
  dojo.byId('delegateId').value = id;
  cmr.showConfirm('actualRemoveDelegate()', 'Remove <strong>' + name + '</strong> from the Delegate List?');
}

/**
 * Called after confirm
 */
function actualRemoveDelegate() {
  FormManager.doHiddenAction('frmCMRDel', 'REMOVE_DELEGATE', cmr.CONTEXT_ROOT + '/preferences/delegate.json', true, refreshAfterResult);
  dojo.byId('delegateId').value = '';
  dojo.byId('delegateNm').value = '';
}

/**
 * Adds the manager to the delegate list
 */
function doAddManager() {
  cmr.showConfirm('actualAddManager()', 'Add <strong>' + FormManager.getActualValue('managerName') + '</strong> to the Delegate List?');
}

/**
 * Called after the confirm
 */
function actualAddManager() {
  FormManager.doHiddenAction('frmCMRDel', 'ADD_MGR', cmr.CONTEXT_ROOT + '/preferences/delegate.json', true, refreshAfterResult);
}

/**
 * Formatter for the Action column of the Delegate list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function removeFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(1);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var delegateId = rowData.delegateId;
  var delegateNm = rowData.delegateNm;
  return '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveDelegate(\'' + delegateId + '\',\'' + delegateNm + '\')">';
}

/**
 * Formatter for the Action column of the Delegate list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function removeCntryFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var userId = rowData.requesterId;
  var cntry = rowData.issuingCntry;
  return '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveCntry(\'' + userId + '\',\'' + cntry + '\')">';
}

/**
 * Removes the delegate with the given ID and name
 * 
 * @param id
 * @param name
 */
function doRemoveCntry(id, name) {
  FormManager.setValue('removeCntry', name);
  cmr.showConfirm('actualRemoveCntry()', 'Remove <strong>' + name + '</strong> from the Preferred Countries List?');
}

/**
 * Called after confirm
 */
function actualRemoveCntry() {
  FormManager.doHiddenAction('frmCMRCntry', 'REMOVE_CNTRY', cmr.CONTEXT_ROOT + '/preferences/cntry.json', true, refreshAfterCntryResult);
  FormManager.setValue('removeCntry', '');
}

/**
 * 
 * @param result
 */
function refreshAfterCntryResult(result) {
  if (result.success) {
    CmrGrid.refresh('PREF_CNTRY_GRID');
  }
}

/**
 * Adds a delegate to the list
 */
function doAddCntry() {
  if (dijit.byId('issuingCntry').get('value') == '') {
    cmr.showAlert('Select a country from the list.');
    return;
  }
  cmr.showConfirm('actualAddCntry()', 'Add <strong>' + dijit.byId('issuingCntry').get('value') + '</strong> to the Preferred Country List?');
}

/**
 * Called after the confirm
 */
function actualAddCntry() {
  FormManager.doHiddenAction('frmCMRCntry', 'ADD_CNTRY', cmr.CONTEXT_ROOT + '/preferences/cntry.json', false, refreshAfterCntryResult);
}
