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
        var iterId = FormManager.getActualValue('iterId');
        var dplPassed = true;
        var params = {
          _qall : 'Y',
          ITER_ID : iterId,
          REQ_ID : reqId
        };

        var dplResult = cmr.query('ND.MASS_ADDR.DPL_CHECK', params);
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
  GEOHandler.NORDX = [ '846', '806', '702', '678' ];
  console.log('adding Mass Change FR validators...');

  GEOHandler.registerValidator(addDPLValidator, GEOHandler.NORDX, null, true);
});