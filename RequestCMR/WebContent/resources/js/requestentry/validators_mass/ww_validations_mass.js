/*
 * File: ww_validations_mass.js
 * Description:
 * Contains the functions necessary to handle country/GEO specific validations 
 * for the mass change UI.
 */

/* Register WW Validators */
dojo.addOnLoad(function() {
  console.log('adding Mass Change WW validators...');
  GEOHandler.LA = [ '613', '629', '631', '655', '661', '663', '681', '683', '731', '735', '781', '799', '811', '813', '815', '829', '869', '871' ];

});