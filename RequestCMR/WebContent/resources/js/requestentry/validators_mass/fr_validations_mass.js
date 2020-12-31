/*
 * File: fr_validations_mass.js
 * Description:
 * Contains the functions necessary to handle FR specific validations 
 * for the mass change UI.
 */

/* Register FR Validators */

function addDPLValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var role = null;
        if (typeof (_pagemodel) != 'undefined') {
          role = _pagemodel.userRole;
        }
        if (role != 'Processor') {
          return new ValidationResult(null, true, null);
        }

        var reqId = FormManager.getActualValue('reqId');
        var dplPassed = true;
        var params = {
          _qall : 'Y',
          REQ_ID : reqId
        };

        var dplResult = cmr.query('FR.MASS_ADDR.DPL_CHECK', params);
        for (var i = 0; i < dplResult.length; i++) {
          dpl = dplResult[i].ret1;
          if (dpl != 'P') {
            dplPassed = false;
            break;
          }
        }

        if (!dplPassed) {
          return new ValidationResult(null, false, 'DPL Check has failed or not completed. This record cannot be processed.');
        } else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), null, 'frmCMR');
}

dojo.addOnLoad(function() {
  GEOHandler.FR = [ SysLoc.FRANCE ];
  console.log('adding Mass Change FR validators...');

  GEOHandler.registerValidator(addDPLValidator, GEOHandler.FR, null, true);
});