/*
 * File: notify.js
 * Description: 
 * Contains the functions for the notify tab
 * 
 */

/**
 * Formatter for Notify List Actions column
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function notifyListActionsFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  if (rowData.removable == 'N') {
    return ''; // requester, cannot be removed
  }
  var notifId = rowData.notifId;
  var notifNm = rowData.notifNm;
  return '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveFromNotifyList(\'' + notifId + '\',\'' + notifNm + '\')">';
}

/**
 * Adds to the notify list
 */
function doAddToNotifyList() {
  if (FormManager.validate('frmCMRNotif')) {
    cmr.showConfirm('actualAddToNotifyList()', 'Add <strong>' + FormManager.getActualValue('notifNm') + '</strong> to the Notify List?');
  }
}

/**
 * Does the actual adding to the list
 */
function actualAddToNotifyList() {
  cmr.notifyReqId = FormManager.getActualValue('reqId');
  FormManager.doHiddenAction('frmCMRNotif', 'ADD_NOTIFY', cmr.CONTEXT_ROOT + '/request/notify/process.json?reqId=' + cmr.notifyReqId, false,
      refreshNotifyAfterResult);
}

/**
 * Removes an entry from the notify list
 */
function doRemoveFromNotifyList(id, name) {
  dojo.byId('notifId').value = id;
  cmr.showConfirm('actualRemoveFromNotifList()', 'Remove <strong>' + name + '</strong> from the Notify List?');
}

/**
 * Does the actual removal from the list
 */
function actualRemoveFromNotifList() {
  cmr.notifyReqId = FormManager.getActualValue('reqId');
  FormManager.doHiddenAction('frmCMRNotif', 'REMOVE_NOTIF', cmr.CONTEXT_ROOT + '/request/notify/process.json?reqId=' + cmr.notifyReqId, true,
      refreshNotifyAfterResult);
}

/**
 * Refresh notify list after processes
 * @param result
 */
function refreshNotifyAfterResult(result){
  if (result.success){
    CmrGrid.refresh('NOTIFY_LIST_GRID');
    dojo.byId('notifNm').value = '';
    dojo.byId('notifId').value = '';
  }
}

