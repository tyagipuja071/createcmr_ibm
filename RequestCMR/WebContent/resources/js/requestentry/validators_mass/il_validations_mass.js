/*
 * File: il_validations_mass.js
 * Description:
 * Contains the functions necessary to handle IL specific validations 
 * for the mass change UI.
 */

/* Register IL Validators */

function addDPLChecklistAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (typeof (_pagemodel) != 'undefined') {
          var reqId = FormManager.getActualValue('reqId');
          var errornames = [];
          var checklistNames = [];
          var qParams = {
            _qall : 'Y',
            REQ_ID : reqId
          };
          var results = cmr.query('MASS.GET.NAMECHANGE.BY_REQID', qParams);
          var resultsChecklist = cmr.query('GET_DPL_CHECKLIST_ATTACHMENT', qParams);
          if (resultsChecklist != null) {
            for (var i = 0; i < resultsChecklist.length; i++) {
              checklistNames.push(resultsChecklist[i].ret1);
              checklistNames.push(resultsChecklist[i].ret2);
            }
          }

          if (results != null) {
            for (var i = 0; i < results.length; i++) {
              if (!checklistNames.toString().includes(results[i].ret1 + results[i].ret2)) {
                errornames.push(results[i].ret1 + ' ' + results[i].ret2);
              }
            }
          }
          if (errornames.length > 0) {
            return new ValidationResult(null, false, 'The following customer name was updated: ' + errornames.toString() + ', please attach checklist');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {
  GEOHandler.IL = [ SysLoc.ISRAEL ];
  console.log('adding Mass Change IL validators...');

  GEOHandler.registerValidator(addDPLChecklistAttachmentValidator, GEOHandler.IL, null, true);
});