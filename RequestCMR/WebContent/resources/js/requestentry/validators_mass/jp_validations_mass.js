/*
 * File: jp_validations_mass.js
 * Description:
 * Contains the functions necessary to handle JP specific validations 
 * for the mass change UI.
 */

/* Register JP Validators */

/* Override Actions for Approvals */
/**
 * Formatter for the Action column of the Approval list grid
 * 
 */
function actionsFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var status = rowData.status;
  var requiredIndc = rowData.requiredIndc.toString();
  var actions = '';
  var approvalId = rowData.approvalId[0];
  var approverNm = rowData.approverNm[0];
  if (status == 'DRA') {
    _approvals.hasDraft = true;
  }
  if ('PAPR' == status) {
    actions = actions + '<input type="button" value="Send Reminder" class="cmr-grid-btn" onclick="sendApprovalReminder(\'' + approvalId + '\', \'' + approverNm + '\', \'' + status + '\')">';
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status + '\', \'' + requiredIndc + '\')">';
  }
  if ('DRA' == status || 'PMAIL' == status || 'PAPR' == status || 'OVERP' == status || 'PREM' == status) {
    if (!('Y' == requiredIndc.toUpperCase())) {
      actions = actions + '<input type="button" value="Cancel" class="cmr-grid-btn" onclick="cancelApproval(\'' + approvalId + '\', \'' + status + '\')">';
    } else if ('PROCESSOR' == role.toUpperCase()) {
      actions = actions + '<input type="button" value="Cancel" class="cmr-grid-btn" onclick="cancelApproval(\'' + approvalId + '\', \'' + status + '\')">';
    }
  }
  if ('REJ' == status) {
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status + '\', \'' + requiredIndc + '\')">'
        + '<input type="button" value="Re-submit" class="cmr-grid-btn" onclick="resubmitApproval(\'' + approvalId + '\', \'' + status + '\')">';
  }
  if ('DRA' == status) {
    // actions = actions + '<input type="button" value="Send Request"
    // class="cmr-grid-btn" onclick="sendApprovalRequest(\'' + approvalId + '\',
    // \'' + approverNm + '\', \'' + status + '\')">';
  }

  return actions;
}

function actionsFormatterBlank(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var status = rowData.status[0];
  var approvalId = rowData.approvalId[0];
  var approverNm = rowData.approverNm[0];
  var actions = '';
  var viewer = true;
  if ((typeof (_pagemodel) != 'undefined') && _pagemodel.userRole != 'Viewer') {
    viewer = false;
  }
  if (!viewer && status == 'PAPR') {
    actions = actions + '<input type="button" value="Send Reminder" class="cmr-grid-btn" onclick="sendApprovalReminder(\'' + approvalId + '\', \'' + approverNm + '\', \'' + status + '\')">';
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status + '\', \'' + requiredIndc + '\')">';
  }
  return actions;
}

function afterConfigForJPMass() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('requestDueDateTemp');
  }

  disableAutoProcForProcessor();
}

function addDateValidatorForReqDueDate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqDueDate = FormManager.getActualValue('requestDueDateTemp');
        if (!isValidDate(reqDueDate))
          return new ValidationResult(FormManager.getField('requestDueDateTemp'), false, 'Request Due Date should be in date format, like YYYY-MM-DD.');
        else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}
function isValidDate(dateString) {
  if (dateString == '' || dateString == null)
    return true;
  if (dateString.length > 10)
    return false;

  if (!/^(\d{1,4})(-)(\d{1,2})\2(\d{1,2})$/.test(dateString)) {
    return false;
  }

  var year = parseInt(dateString.substr(0, 4), 10);
  var month = parseInt(dateString.substr(5, 2), 10);
  var day = parseInt(dateString.substr(8, 2), 10);

  // Check the ranges of month and year
  if (year == 0 || month == 0 || month > 12)
    return false;
  if (month == 2) {
    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
      if (day == 0 || day > 29)
        return false;
    } else {
      if (day == 0 || day > 28)
        return false;
    }
  }
  if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
    if (day == 0 || day > 31)
      return false;
  }
  if (month == 4 || month == 6 || month == 9 || month == 11) {
    if (day == 0 || day > 30)
      return false;
  }
  return true;
}

function disableAutoProcForProcessor() {
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }
  if (_role == 'Processor') {
    FormManager.getField('disableAutoProc').set('checked', true);
    FormManager.readOnly('disableAutoProc');
  }
}

dojo.addOnLoad(function() {
  GEOHandler.JP = [ SysLoc.JAPAN ];
  console.log('adding Mass Change JP validators...');

  GEOHandler.addAfterConfig(afterConfigForJPMass, GEOHandler.JP);
  GEOHandler.registerValidator(addDateValidatorForReqDueDate, GEOHandler.JP, null, true);
});