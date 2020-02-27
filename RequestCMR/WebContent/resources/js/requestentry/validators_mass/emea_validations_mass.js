/*
 * File: emea_validations_mass.js
 * Description:
 * Contains the functions necessary to handle EMEA specific validations 
 * for the mass change UI.
 */

var EMEA_COUNTRIES = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];

var EMEAMassHandler = {
  isEMEAIssuingCountry : function() {
    var sysLocCd = FormManager.getActualValue('cmrIssuingCntry');
    if (EMEA_COUNTRIES.indexOf(sysLocCd) > -1) {
      return true;
    } else {
      return false;
    }
  },

};

/**
 * Validator to check whether requester is pre-defined requester for special
 * Mass Update Requests
 */
function preDefinedRequesterValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requesterId = FormManager.getActualValue('requesterId');
        var reqReason = FormManager.getActualValue('reqReason');
        if (reqReason == 'MOVE' || reqReason == 'PB') {
          var paramCode = "PDR_" + reqReason;
          var qParams = {
            PARAMETER_CD : paramCode
          };
          var result = cmr.query('GET_PREDEFINED_REQUESTERS_MU', qParams);
          if (result != null && result.ret1 != '' && result.ret1 != undefined) {
            var preDefinedRequesters = result.ret1;
            var items = preDefinedRequesters.toLowerCase().split(/\s*,\s*/);
            var isContained = items.indexOf(requesterId) > -1;
            if (isContained == true)
              return new ValidationResult(null, true);
            else {
              if (reqReason == 'MOVE') {
                return new ValidationResult(null, false,
                    'You are not authorized to select Request Reason "Semi-annual account move process" for Mass-Update. Select another Request Reason or contact Zuzana Lasakova/Slovakia/IBM.');
              } else if (reqReason == 'PB') {
                return new ValidationResult(null, false,
                    'You are not authorized to select Request Reason "Project Based" for Mass-Update. Select another Request Reason or contact James M Balfe/UK/IBM.');
              }
            }
          } else {
            return new ValidationResult(null, false, 'The request reason is applicable only for pre defined requesters.Please define pre-defined requesters via Sytem Parameters.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

/* Register EMEA Validators */
dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS ];
  console.log('adding Mass Change EMEA validators...');
  GEOHandler.registerValidator(preDefinedRequesterValidator, GEOHandler.EMEA, null, false, false);

});